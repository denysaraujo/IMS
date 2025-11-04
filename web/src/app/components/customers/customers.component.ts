import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { CustomerService, Customer, CustomerStats, Address } from '../../services/customer.service';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customers.component.html',
  styleUrls: ['./customers.component.css']
})
export class CustomersComponent implements OnInit {
  currentUser: any = null;
  customers: Customer[] = [];
  filteredCustomers: Customer[] = [];
  customerStats: CustomerStats = { totalCustomers: 0, individualCustomers: 0, companyCustomers: 0 };
  searchTerm: string = '';
  selectedCustomer: Customer | null = null;
  isEditing: boolean = false;
  isCreating: boolean = false;
  isLoading: boolean = false;
  errorMessage: string = '';

  // Novo cliente para formulário
  newCustomer: Customer = {
    name: '',
    document: '',
    email: '',
    phone: '',
    type: 'INDIVIDUAL',
    address: {
      street: '',
      number: '',
      complement: '',
      neighborhood: '',
      city: '',
      state: '',
      zipCode: '',
      country: 'Brasil'
    }
  };

  // Estados brasileiros para o formulário
  estados = [
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 
    'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 
    'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
  ];

  // Limites de caracteres
  readonly DOCUMENT_LIMITS = {
    CPF: 14, // 000.000.000-00
    CNPJ: 18 // 00.000.000/0000-00
  };

  readonly PHONE_LIMITS = {
    PHONE: 14, // (00) 0000-0000
    CELLPHONE: 15 // (00) 00000-0000
  };

  constructor(
    private authService: AuthService,
    private customerService: CustomerService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadCustomers();
    this.loadCustomerStats();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadCustomers() {
    this.isLoading = true;
    this.customerService.getCustomers().subscribe({
      next: (customers) => {
        this.customers = customers;
        this.filteredCustomers = customers;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar clientes:', error);
        this.errorMessage = 'Erro ao carregar lista de clientes';
        this.isLoading = false;
      }
    });
  }

  loadCustomerStats() {
    this.customerService.getCustomerStats().subscribe({
      next: (stats) => {
        this.customerStats = stats;
      },
      error: (error) => {
        console.error('Erro ao carregar estatísticas:', error);
        this.errorMessage = 'Erro ao carregar estatísticas';
      }
    });
  }

  get activeCustomersCount(): number {
    return this.customers.filter(c => (c.totalOrders || 0) > 0).length;
  }

  searchCustomers() {
    if (!this.searchTerm.trim()) {
      this.filteredCustomers = this.customers;
      return;
    }

    this.filteredCustomers = this.customers.filter(customer =>
      customer.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      customer.document.includes(this.searchTerm) ||
      customer.email?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      customer.phone?.includes(this.searchTerm)
    );
  }

  selectCustomer(customer: Customer) {
    this.isLoading = true;
    this.customerService.getCustomer(customer.id!).subscribe({
      next: (fullCustomer) => {
        this.selectedCustomer = fullCustomer;
        this.isEditing = false;
        this.isCreating = false;
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar detalhes do cliente:', error);
        this.errorMessage = 'Erro ao carregar detalhes do cliente';
        this.isLoading = false;
      }
    });
  }

  startCreate() {
    this.newCustomer = {
      name: '',
      document: '',
      email: '',
      phone: '',
      type: 'INDIVIDUAL',
      address: {
        street: '',
        number: '',
        complement: '',
        neighborhood: '',
        city: '',
        state: '',
        zipCode: '',
        country: 'Brasil'
      }
    };
    this.isCreating = true;
    this.isEditing = false;
    this.selectedCustomer = null;
    this.errorMessage = '';
  }

  startEdit() {
    if (this.selectedCustomer) {
      // Busca os dados completos do cliente antes de editar
      this.isLoading = true;
      this.customerService.getCustomer(this.selectedCustomer.id!).subscribe({
        next: (customer) => {
          this.newCustomer = { 
            ...customer,
            address: customer.address ? { ...customer.address } : {
              street: '', number: '', complement: '', neighborhood: '',
              city: '', state: '', zipCode: '', country: 'Brasil'
            }
          };
          this.isEditing = true;
          this.isCreating = false;
          this.errorMessage = '';
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erro ao carregar dados do cliente:', error);
          this.errorMessage = 'Erro ao carregar dados do cliente';
          this.isLoading = false;
        }
      });
    }
  }

  validateCustomer(): boolean {
    if (!this.newCustomer.name.trim()) {
      this.errorMessage = 'Nome é obrigatório';
      return false;
    }

    if (!this.newCustomer.document.trim()) {
      this.errorMessage = 'Documento é obrigatório';
      return false;
    }

    // Validação básica de CPF/CNPJ
    const doc = this.newCustomer.document.replace(/\D/g, '');
    if (this.newCustomer.type === 'INDIVIDUAL' && doc.length !== 11) {
      this.errorMessage = 'CPF deve ter 11 dígitos';
      return false;
    }
    if (this.newCustomer.type === 'COMPANY' && doc.length !== 14) {
      this.errorMessage = 'CNPJ deve ter 14 dígitos';
      return false;
    }

    if (this.newCustomer.email && !this.isValidEmail(this.newCustomer.email)) {
      this.errorMessage = 'Email inválido';
      return false;
    }

    return true;
  }

  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  formatDocument() {
    if (!this.newCustomer.document) return;
    
    // Remove caracteres não numéricos
    let doc = this.newCustomer.document.replace(/\D/g, '');
    
    if (this.newCustomer.type === 'INDIVIDUAL') {
      // CPF: limita a 11 dígitos
      if (doc.length > 11) {
        doc = doc.substring(0, 11);
      }
      
      // Formata CPF: 000.000.000-00
      if (doc.length <= 11) {
        if (doc.length <= 3) {
          this.newCustomer.document = doc;
        } else if (doc.length <= 6) {
          this.newCustomer.document = doc.replace(/(\d{3})(\d{0,3})/, '$1.$2');
        } else if (doc.length <= 9) {
          this.newCustomer.document = doc.replace(/(\d{3})(\d{3})(\d{0,3})/, '$1.$2.$3');
        } else {
          this.newCustomer.document = doc.replace(/(\d{3})(\d{3})(\d{3})(\d{0,2})/, '$1.$2.$3-$4');
        }
      }
    } else {
      // CNPJ: limita a 14 dígitos
      if (doc.length > 14) {
        doc = doc.substring(0, 14);
      }
      
      // Formata CNPJ: 00.000.000/0000-00
      if (doc.length <= 14) {
        if (doc.length <= 2) {
          this.newCustomer.document = doc;
        } else if (doc.length <= 5) {
          this.newCustomer.document = doc.replace(/(\d{2})(\d{0,3})/, '$1.$2');
        } else if (doc.length <= 8) {
          this.newCustomer.document = doc.replace(/(\d{2})(\d{3})(\d{0,3})/, '$1.$2.$3');
        } else if (doc.length <= 12) {
          this.newCustomer.document = doc.replace(/(\d{2})(\d{3})(\d{3})(\d{0,4})/, '$1.$2.$3/$4');
        } else {
          this.newCustomer.document = doc.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{0,2})/, '$1.$2.$3/$4-$5');
        }
      }
    }
  }

  formatPhone() {
    if (!this.newCustomer.phone) return;
    
    // Remove caracteres não numéricos
    let phone = this.newCustomer.phone.replace(/\D/g, '');
    
    // Limita o número de dígitos (máximo 11 para celular com DDD)
    if (phone.length > 11) {
      phone = phone.substring(0, 11);
    }
    
    // Aplica a formatação baseada no tamanho
    if (phone.length === 0) {
      return;
    } else if (phone.length <= 2) {
      // Apenas DDD
      this.newCustomer.phone = `(${phone}`;
    } else if (phone.length <= 6) {
      // DDD + parte do número
      this.newCustomer.phone = phone.replace(/(\d{2})(\d{0,4})/, '($1) $2');
    } else if (phone.length <= 10) {
      // Telefone fixo: (00) 0000-0000
      this.newCustomer.phone = phone.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    } else {
      // Celular: (00) 00000-0000
      this.newCustomer.phone = phone.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
    }
  }

  // Método para aplicar máscara dinâmica no input
  onDocumentInput(event: any) {
    this.formatDocument();
    // Aplica limite máximo baseado no tipo
    const maxLength = this.newCustomer.type === 'INDIVIDUAL' ? 
      this.DOCUMENT_LIMITS.CPF : this.DOCUMENT_LIMITS.CNPJ;
    
    if (this.newCustomer.document.length > maxLength) {
      this.newCustomer.document = this.newCustomer.document.substring(0, maxLength);
    }
  }

  onPhoneInput(event: any) {
    this.formatPhone(); // formatPhone já lida com o caso de ser undefined

    // Se after formatPhone, this.newCustomer.phone for undefined, não fazemos nada
    if (!this.newCustomer.phone) {
      return;
    }

    // Determina o limite máximo baseado no formato (celular ou telefone)
    const cleanedPhone = this.newCustomer.phone.replace(/\D/g, '');
    const isCellphone = cleanedPhone.length === 11;
    const maxLength = isCellphone ? this.PHONE_LIMITS.CELLPHONE : this.PHONE_LIMITS.PHONE;

    if (this.newCustomer.phone.length > maxLength) {
      this.newCustomer.phone = this.newCustomer.phone.substring(0, maxLength);
    }
}

  // Método para validar o formato do telefone
  isValidPhoneFormat(phone: string): boolean {
    const cleaned = phone.replace(/\D/g, '');
    return cleaned.length === 10 || cleaned.length === 11;
  }

  // Método corrigido para formatar CEP
  formatZipCode() {
    // Verifica se address existe, se não, cria um objeto vazio
    if (!this.newCustomer.address) {
      this.newCustomer.address = {
        street: '',
        number: '',
        complement: '',
        neighborhood: '',
        city: '',
        state: '',
        zipCode: '',
        country: 'Brasil'
      };
      return;
    }
    
    if (!this.newCustomer.address.zipCode) return;
    
    let zipCode = this.newCustomer.address.zipCode.replace(/\D/g, '');
    
    // Limita a 8 dígitos
    if (zipCode.length > 8) {
      zipCode = zipCode.substring(0, 8);
    }
    
    // Formata: 00000-000
    if (zipCode.length <= 5) {
      this.newCustomer.address.zipCode = zipCode;
    } else {
      this.newCustomer.address.zipCode = zipCode.replace(/(\d{5})(\d{0,3})/, '$1-$2');
    }
  }

  saveCustomer() {
    if (!this.validateCustomer()) {
      return;
    }

    // Validação adicional do telefone
    if (this.newCustomer.phone && !this.isValidPhoneFormat(this.newCustomer.phone)) {
      this.errorMessage = 'Telefone deve ter 10 ou 11 dígitos (com DDD)';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Remove formatação antes de enviar para o backend
    const customerToSave: Customer = {
      ...this.newCustomer,
      document: this.newCustomer.document.replace(/\D/g, ''),
      phone: this.newCustomer.phone ? this.newCustomer.phone.replace(/\D/g, '') : ''
    };

    // Se existir address, remove a formatação do CEP também
    if (customerToSave.address && customerToSave.address.zipCode) {
      customerToSave.address.zipCode = customerToSave.address.zipCode.replace(/\D/g, '');
    }

    if (this.isCreating) {
      this.customerService.createCustomer(customerToSave).subscribe({
        next: (customer) => {
          this.loadCustomers();
          this.loadCustomerStats();
          this.cancelEdit();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erro ao criar cliente:', error);
          this.errorMessage = error.error?.message || 'Erro ao criar cliente';
          this.isLoading = false;
        }
      });
    } else if (this.isEditing && this.selectedCustomer) {
      this.customerService.updateCustomer(this.selectedCustomer.id!, customerToSave).subscribe({
        next: (customer) => {
          this.loadCustomers();
          this.cancelEdit();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erro ao atualizar cliente:', error);
          this.errorMessage = error.error?.message || 'Erro ao atualizar cliente';
          this.isLoading = false;
        }
      });
    }
  }

  cancelEdit() {
    this.isCreating = false;
    this.isEditing = false;
    this.selectedCustomer = null;
    this.newCustomer = {
      name: '',
      document: '',
      email: '',
      phone: '',
      type: 'INDIVIDUAL',
      address: {
        street: '',
        number: '',
        complement: '',
        neighborhood: '',
        city: '',
        state: '',
        zipCode: '',
        country: 'Brasil'
      }
    };
    this.errorMessage = '';
  }

  deleteCustomer(customer: Customer) {
    if (confirm(`Tem certeza que deseja excluir o cliente ${customer.name}?`)) {
      this.isLoading = true;
      this.customerService.deleteCustomer(customer.id!).subscribe({
        next: () => {
          this.loadCustomers();
          this.loadCustomerStats();
          if (this.selectedCustomer?.id === customer.id) {
            this.selectedCustomer = null;
          }
          this.isLoading = false;
          // Feedback visual de sucesso
          this.showSuccessMessage('Cliente excluído com sucesso!');
        },
        error: (error) => {
          console.error('Erro ao excluir cliente:', error);
          this.errorMessage = error.error?.message || 'Erro ao excluir cliente. Verifique se o cliente não possui pedidos vinculados.';
          this.isLoading = false;
        }
      });
    }
  }

  showSuccessMessage(message: string) {
    // Você pode implementar um toast ou notificação aqui
    console.log(message);
    // Exemplo simples com alert
    alert(message);
  }

  getCustomerTypeText(type: string): string {
    return type === 'INDIVIDUAL' ? 'Pessoa Física' : 'Pessoa Jurídica';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value || 0);
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  // Método para detectar automaticamente o tipo de documento
  onDocumentTypeChange() {
    // Limpa e reformata o documento quando o tipo muda
    if (this.newCustomer.document) {
      this.newCustomer.document = this.newCustomer.document.replace(/\D/g, '');
      this.formatDocument();
    }
  }

  // Método para detectar automaticamente se é celular ou telefone
  detectPhoneType(phone: string): 'PHONE' | 'CELLPHONE' {
    const cleaned = phone.replace(/\D/g, '');
    return cleaned.length === 11 ? 'CELLPHONE' : 'PHONE';
  }
}
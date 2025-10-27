import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UtilsService {
  
  // Máscara para telefone (11) 9999-9999
  formatPhone(value: string): string {
    if (!value) return '';
    
    // Remove tudo que não é número
    const numbers = value.replace(/\D/g, '');
    
    // Aplica a máscara baseada no tamanho
    if (numbers.length <= 10) {
      return numbers
        .replace(/(\d{2})/, '($1) ')
        .replace(/(\d{2})(\d{4})/, '($1) $2-')
        .replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    } else {
      // Para números com DDI
      return numbers
        .replace(/(\d{2})/, '+$1 ')
        .replace(/(\d{2})(\d{2})/, '+$1 ($2) ')
        .replace(/(\d{2})(\d{2})(\d{4})/, '+$1 ($2) $3-')
        .replace(/(\d{2})(\d{2})(\d{4})(\d{4})/, '+$1 ($2) $3-$4');
    }
  }

  // Máscara para celular (11) 99999-9999
  formatCellphone(value: string): string {
    if (!value) return '';
    
    // Remove tudo que não é número
    const numbers = value.replace(/\D/g, '');
    
    // Aplica a máscara
    if (numbers.length <= 11) {
      return numbers
        .replace(/(\d{2})/, '($1) ')
        .replace(/(\d{2})(\d{5})/, '($1) $2-')
        .replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    } else {
      // Para números com DDI
      return numbers
        .replace(/(\d{2})/, '+$1 ')
        .replace(/(\d{2})(\d{2})/, '+$1 ($2) ')
        .replace(/(\d{2})(\d{2})(\d{5})/, '+$1 ($2) $3-')
        .replace(/(\d{2})(\d{2})(\d{5})(\d{4})/, '+$1 ($2) $3-$4');
    }
  }

  // Remove a máscara para salvar apenas números
  removeMask(value: string): string {
    return value ? value.replace(/\D/g, '') : '';
  }

  // Validação de email
  isValidEmail(email: string): boolean {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
  }

  // Formata email em tempo real (apenas valida)
  formatEmail(value: string): string {
    return value.toLowerCase();
  }
}
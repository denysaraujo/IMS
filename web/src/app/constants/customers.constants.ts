// customers.constants.ts

// Estados brasileiros
export const BRAZILIAN_STATES = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 
  'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 
  'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
] as const;

// Tipos de cliente
export const CUSTOMER_TYPES = {
  INDIVIDUAL: 'INDIVIDUAL',
  COMPANY: 'COMPANY'
} as const;

export type CustomerType = typeof CUSTOMER_TYPES[keyof typeof CUSTOMER_TYPES];

// Labels para exibição
export const CUSTOMER_TYPE_LABELS = {
  [CUSTOMER_TYPES.INDIVIDUAL]: 'Pessoa Física',
  [CUSTOMER_TYPES.COMPANY]: 'Pessoa Jurídica'
} as const;

// Limites de caracteres para documentos
export const DOCUMENT_LIMITS = {
  CPF: 14,  // 000.000.000-00
  CNPJ: 18  // 00.000.000/0000-00
} as const;

// Limites de caracteres para telefone
export const PHONE_LIMITS = {
  PHONE: 14,     // (00) 0000-0000
  CELLPHONE: 15  // (00) 00000-0000
} as const;

// Máscaras para documentos
export const DOCUMENT_MASKS = {
  CPF: '000.000.000-00',
  CNPJ: '00.000.000/0000-00'
} as const;

// // Mensagens de validação
// export const CUSTOMER_VALIDATION_MESSAGES = {
//   REQUIRED_NAME: 'Nome é obrigatório',
//   REQUIRED_DOCUMENT: 'Documento é obrigatório',
//   INVALID_CPF: 'CPF inválido',
//   INVALID_CNPJ: 'CNPJ inválido',
//   INVALID_EMAIL: 'E-mail inválido',
//   INVALID_PHONE: 'Telefone inválido'
// } as const;

// // Opções para selects
// export const CUSTOMER_TYPE_OPTIONS = [
//   { value: CUSTOMER_TYPES.INDIVIDUAL, label: CUSTOMER_TYPE_LABELS[CUSTOMER_TYPES.INDIVIDUAL], icon: 'fas fa-user' },
//   { value: CUSTOMER_TYPES.COMPANY, label: CUSTOMER_TYPE_LABELS[CUSTOMER_TYPES.COMPANY], icon: 'fas fa-building' }
// ] as const;

// // Estados para selects (com nome completo)
// export const BRAZILIAN_STATES_FULL = [
//   { code: 'AC', name: 'Acre' },
//   { code: 'AL', name: 'Alagoas' },
//   { code: 'AP', name: 'Amapá' },
//   { code: 'AM', name: 'Amazonas' },
//   { code: 'BA', name: 'Bahia' },
//   { code: 'CE', name: 'Ceará' },
//   { code: 'DF', name: 'Distrito Federal' },
//   { code: 'ES', name: 'Espírito Santo' },
//   { code: 'GO', name: 'Goiás' },
//   { code: 'MA', name: 'Maranhão' },
//   { code: 'MT', name: 'Mato Grosso' },
//   { code: 'MS', name: 'Mato Grosso do Sul' },
//   { code: 'MG', name: 'Minas Gerais' },
//   { code: 'PA', name: 'Pará' },
//   { code: 'PB', name: 'Paraíba' },
//   { code: 'PR', name: 'Paraná' },
//   { code: 'PE', name: 'Pernambuco' },
//   { code: 'PI', name: 'Piauí' },
//   { code: 'RJ', name: 'Rio de Janeiro' },
//   { code: 'RN', name: 'Rio Grande do Norte' },
//   { code: 'RS', name: 'Rio Grande do Sul' },
//   { code: 'RO', name: 'Rondônia' },
//   { code: 'RR', name: 'Roraima' },
//   { code: 'SC', name: 'Santa Catarina' },
//   { code: 'SP', name: 'São Paulo' },
//   { code: 'SE', name: 'Sergipe' },
//   { code: 'TO', name: 'Tocantins' }
// ] as const;

// // URLs de APIs (se necessário)
// export const CUSTOMER_API_ENDPOINTS = {
//   BASE: '/api/customers',
//   STATS: '/api/customers/stats',
//   SEARCH: '/api/customers/search'
// } as const;

// // Configurações de paginação
// export const CUSTOMER_PAGINATION = {
//   DEFAULT_PAGE_SIZE: 10,
//   PAGE_SIZE_OPTIONS: [5, 10, 25, 50, 100]
// } as const;

// // Função auxiliar para obter label do tipo de cliente
// export function getCustomerTypeLabel(type: CustomerType): string {
//   return CUSTOMER_TYPE_LABELS[type] || type;
// }

// // Função auxiliar para obter ícone do tipo de cliente
// export function getCustomerTypeIcon(type: CustomerType): string {
//   const option = CUSTOMER_TYPE_OPTIONS.find(opt => opt.value === type);
//   return option?.icon || 'fas fa-user';
// }

// // Função auxiliar para obter limite de documento baseado no tipo
// export function getDocumentLimit(type: CustomerType): number {
//   return type === CUSTOMER_TYPES.INDIVIDUAL ? DOCUMENT_LIMITS.CPF : DOCUMENT_LIMITS.CNPJ;
// }

// // Função auxiliar para obter máscara de documento baseado no tipo
// export function getDocumentMask(type: CustomerType): string {
//   return type === CUSTOMER_TYPES.INDIVIDUAL ? DOCUMENT_MASKS.CPF : DOCUMENT_MASKS.CNPJ;
// }
export type TipoEventoCorporativo = 'BONIFICACAO' | 'DESDOBRAMENTO' | 'GRUPAMENTO';

export interface TipoEventoCorporativoOption {
  value: TipoEventoCorporativo;
  label: string;
  ajuda: string;
}

export const TIPOS_EVENTO_CORPORATIVO: TipoEventoCorporativoOption[] = [
  {
    value: 'BONIFICACAO',
    label: 'Bonificação',
    ajuda: 'Cotas grátis. Fator = proporção (0.10 = +10%).'
  },
  {
    value: 'DESDOBRAMENTO',
    label: 'Desdobramento',
    ajuda: 'Cota vira várias. Fator = multiplicador (10 = 1:10).'
  },
  {
    value: 'GRUPAMENTO',
    label: 'Grupamento',
    ajuda: 'Várias viram uma. Fator = divisor (10 = 10:1).'
  }
];

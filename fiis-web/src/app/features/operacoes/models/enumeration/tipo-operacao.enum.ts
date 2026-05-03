export type TipoOperacao = 'COMPRA' | 'VENDA';

export interface TipoOperacaoOption {
  value: TipoOperacao;
  label: string;
}

export const TIPOS_OPERACAO: TipoOperacaoOption[] = [
  { value: 'COMPRA', label: 'Compra' },
  { value: 'VENDA', label: 'Venda' }
];

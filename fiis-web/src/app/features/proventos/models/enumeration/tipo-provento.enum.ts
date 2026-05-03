export type TipoProvento = 'RENDIMENTO' | 'AMORTIZACAO';

export interface TipoProventoOption {
  value: TipoProvento;
  label: string;
}

export const TIPOS_PROVENTO: TipoProventoOption[] = [
  { value: 'RENDIMENTO', label: 'Rendimento' },
  { value: 'AMORTIZACAO', label: 'Amortização' }
];

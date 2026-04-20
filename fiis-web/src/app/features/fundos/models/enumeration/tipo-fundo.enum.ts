export type TipoFundo = 'TIJOLO' | 'PAPEL' | 'HIBRIDO' | 'FUNDO_DE_FUNDOS';

export interface TipoFundoOption {
  value: TipoFundo;
  label: string;
}

export const TIPOS_FUNDO: TipoFundoOption[] = [
  { value: 'TIJOLO', label: 'Tijolo' },
  { value: 'PAPEL', label: 'Papel' },
  { value: 'HIBRIDO', label: 'Híbrido' },
  { value: 'FUNDO_DE_FUNDOS', label: 'Fundo de Fundos' }
];

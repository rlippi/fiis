export type Segmento =
  | 'LOGISTICA'
  | 'SHOPPING'
  | 'LAJES_CORPORATIVAS'
  | 'RESIDENCIAL'
  | 'HOSPITALAR'
  | 'EDUCACIONAL'
  | 'AGENCIAS_BANCARIAS'
  | 'HOTEIS'
  | 'RECEBIVEIS'
  | 'MULTISEGMENTO'
  | 'FUNDO_DE_FUNDOS';

export interface SegmentoOption {
  value: Segmento;
  label: string;
}

export const SEGMENTOS: SegmentoOption[] = [
  { value: 'LOGISTICA', label: 'Logística' },
  { value: 'SHOPPING', label: 'Shopping' },
  { value: 'LAJES_CORPORATIVAS', label: 'Lajes Corporativas' },
  { value: 'RESIDENCIAL', label: 'Residencial' },
  { value: 'HOSPITALAR', label: 'Hospitalar' },
  { value: 'EDUCACIONAL', label: 'Educacional' },
  { value: 'AGENCIAS_BANCARIAS', label: 'Agências Bancárias' },
  { value: 'HOTEIS', label: 'Hotéis' },
  { value: 'RECEBIVEIS', label: 'Recebíveis' },
  { value: 'MULTISEGMENTO', label: 'Multissegmento' },
  { value: 'FUNDO_DE_FUNDOS', label: 'Fundo de Fundos' }
];

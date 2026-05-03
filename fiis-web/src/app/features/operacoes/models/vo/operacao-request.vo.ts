import { TipoOperacao } from '../enumeration/tipo-operacao.enum';

export interface OperacaoRequestVO {
  fundoId: number;
  tipo: TipoOperacao;
  dataOperacao: string;
  quantidade: number;
  precoUnitario: number;
  taxas?: number | null;
  observacao?: string | null;
}

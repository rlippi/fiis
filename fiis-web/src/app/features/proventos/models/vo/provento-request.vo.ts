import { TipoProvento } from '../enumeration/tipo-provento.enum';

export interface ProventoRequestVO {
  fundoId: number;
  tipoProvento: TipoProvento;
  dataReferencia: string;
  dataPagamento: string;
  valorPorCota: number;
  quantidadeCotas: number;
  observacao?: string | null;
}

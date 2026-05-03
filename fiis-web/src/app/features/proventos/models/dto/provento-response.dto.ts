import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';
import { TipoProvento } from '../enumeration/tipo-provento.enum';

export interface ProventoResponseDTO {
  id: number;
  fundo: FundoResumoDTO;
  tipoProvento: TipoProvento;
  tipoProventoDescricao: string;
  dataReferencia: string;
  dataPagamento: string;
  valorPorCota: number;
  quantidadeCotas: number;
  valorTotal: number;
  observacao: string | null;
  dataCriacao: string;
  dataAtualizacao: string;
}

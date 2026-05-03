import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';
import { TipoEventoCorporativo } from '../enumeration/tipo-evento-corporativo.enum';

export interface EventoCorporativoResponseDTO {
  id: number;
  fundo: FundoResumoDTO;
  tipo: TipoEventoCorporativo;
  tipoDescricao: string;
  data: string;
  fator: number;
  descricao: string | null;
  dataCriacao: string;
  dataAtualizacao: string;
}

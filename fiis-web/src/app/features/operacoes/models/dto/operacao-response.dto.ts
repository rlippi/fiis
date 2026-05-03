import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';
import { TipoOperacao } from '../enumeration/tipo-operacao.enum';

export interface OperacaoResponseDTO {
  id: number;
  fundo: FundoResumoDTO;
  tipo: TipoOperacao;
  tipoDescricao: string;
  dataOperacao: string;
  quantidade: number;
  precoUnitario: number;
  taxas: number;
  valorTotal: number;
  observacao: string | null;
  dataCriacao: string;
  dataAtualizacao: string;
}

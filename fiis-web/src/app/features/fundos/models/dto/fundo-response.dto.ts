import { Segmento } from '../enumeration/segmento.enum';
import { TipoFundo } from '../enumeration/tipo-fundo.enum';

export interface FundoResponseDTO {
  id: number;
  ticker: string;
  nome: string;
  cnpj: string | null;
  tipo: TipoFundo;
  tipoDescricao: string;
  segmento: Segmento;
  segmentoDescricao: string;
  ativo: boolean;
  dataCriacao: string;
  dataAtualizacao: string;
}

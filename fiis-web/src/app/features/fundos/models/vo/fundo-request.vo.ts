import { Segmento } from '../enumeration/segmento.enum';
import { TipoFundo } from '../enumeration/tipo-fundo.enum';

export interface FundoRequestVO {
  ticker: string;
  nome: string;
  cnpj?: string | null;
  tipo: TipoFundo;
  segmento: Segmento;
  ativo?: boolean | null;
}

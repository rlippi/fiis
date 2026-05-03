import { TipoEventoCorporativo } from '../enumeration/tipo-evento-corporativo.enum';

export interface EventoCorporativoRequestVO {
  fundoId: number;
  tipo: TipoEventoCorporativo;
  data: string;
  fator: number;
  descricao?: string | null;
}

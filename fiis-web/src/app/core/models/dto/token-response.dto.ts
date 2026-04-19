import { Perfil } from '../enumeration/perfil.enum';

export interface TokenResponse {
  token: string;
  tipo: string;
  nome: string;
  perfil: Perfil;
  expiraEmMs: number;
}

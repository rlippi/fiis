import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';

export interface CotacaoResponseDTO {
  id: number;
  fundo: FundoResumoDTO;
  data: string;
  precoFechamento: number;
  precoAbertura: number | null;
  precoMinimo: number | null;
  precoMaximo: number | null;
  volume: number | null;
  dataCriacao: string;
  dataAtualizacao: string;
}

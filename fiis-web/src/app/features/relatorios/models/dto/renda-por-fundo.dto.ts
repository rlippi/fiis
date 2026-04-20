import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';

export interface RendaPorFundoDTO {
  fundo: FundoResumoDTO;
  totalRecebido: number;
  quantidadeProventos: number;
}

/**
 * Resumo retornado pelo endpoint POST /api/cotacoes/importar-brapi.
 * Indica quantos fundos foram processados, quantas cotações foram criadas
 * ou atualizadas (upsert por fundo + data atual) e lista os tickers que
 * a BRAPI não retornou.
 */
export interface ImportacaoBrapiResponseDTO {
  totalFundos: number;
  criados: number;
  atualizados: number;
  naoEncontradosBrapi: string[];
}

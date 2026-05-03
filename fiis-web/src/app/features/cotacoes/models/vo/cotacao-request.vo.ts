export interface CotacaoRequestVO {
  fundoId: number;
  data: string;
  precoFechamento: number;
  precoAbertura?: number | null;
  precoMinimo?: number | null;
  precoMaximo?: number | null;
  volume?: number | null;
}

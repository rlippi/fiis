import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { AlocacaoDTO } from '../models/dto/alocacao.dto';
import { RendaMensalDTO } from '../models/dto/renda-mensal.dto';
import { RendaPorFundoDTO } from '../models/dto/renda-por-fundo.dto';
import { ResumoCarteiraDTO } from '../models/dto/resumo-carteira.dto';

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/relatorios`;

  resumoCarteira(): Observable<ResumoCarteiraDTO> {
    return this.http.get<ResumoCarteiraDTO>(`${this.baseUrl}/resumo-carteira`);
  }

  alocacaoPorTipo(): Observable<AlocacaoDTO[]> {
    return this.http.get<AlocacaoDTO[]>(`${this.baseUrl}/alocacao-por-tipo`);
  }

  alocacaoPorSegmento(): Observable<AlocacaoDTO[]> {
    return this.http.get<AlocacaoDTO[]>(`${this.baseUrl}/alocacao-por-segmento`);
  }

  rendaMensal(): Observable<RendaMensalDTO[]> {
    return this.http.get<RendaMensalDTO[]>(`${this.baseUrl}/renda-mensal`);
  }

  rendaPorFundo(): Observable<RendaPorFundoDTO[]> {
    return this.http.get<RendaPorFundoDTO[]>(`${this.baseUrl}/renda-por-fundo`);
  }
}

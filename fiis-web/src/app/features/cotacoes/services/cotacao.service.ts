import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { CotacaoResponseDTO } from '../models/dto/cotacao-response.dto';
import { ImportacaoBrapiResponseDTO } from '../models/dto/importacao-brapi-response.dto';
import { CotacaoRequestVO } from '../models/vo/cotacao-request.vo';

@Injectable({ providedIn: 'root' })
export class CotacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/cotacoes`;

  listar(fundoId?: number): Observable<CotacaoResponseDTO[]> {
    let params = new HttpParams();
    if (fundoId != null) {
      params = params.set('fundoId', fundoId);
    }
    return this.http.get<CotacaoResponseDTO[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<CotacaoResponseDTO> {
    return this.http.get<CotacaoResponseDTO>(`${this.baseUrl}/${id}`);
  }

  criar(request: CotacaoRequestVO): Observable<CotacaoResponseDTO> {
    return this.http.post<CotacaoResponseDTO>(this.baseUrl, request);
  }

  atualizar(id: number, request: CotacaoRequestVO): Observable<CotacaoResponseDTO> {
    return this.http.put<CotacaoResponseDTO>(`${this.baseUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  importarBrapi(): Observable<ImportacaoBrapiResponseDTO> {
    return this.http.post<ImportacaoBrapiResponseDTO>(`${this.baseUrl}/importar-brapi`, {});
  }
}

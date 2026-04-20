import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { OperacaoResponseDTO } from '../models/dto/operacao-response.dto';
import { OperacaoRequestVO } from '../models/vo/operacao-request.vo';

@Injectable({ providedIn: 'root' })
export class OperacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/operacoes`;

  listar(fundoId?: number): Observable<OperacaoResponseDTO[]> {
    let params = new HttpParams();
    if (fundoId != null) {
      params = params.set('fundoId', fundoId);
    }
    return this.http.get<OperacaoResponseDTO[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<OperacaoResponseDTO> {
    return this.http.get<OperacaoResponseDTO>(`${this.baseUrl}/${id}`);
  }

  criar(request: OperacaoRequestVO): Observable<OperacaoResponseDTO> {
    return this.http.post<OperacaoResponseDTO>(this.baseUrl, request);
  }

  atualizar(id: number, request: OperacaoRequestVO): Observable<OperacaoResponseDTO> {
    return this.http.put<OperacaoResponseDTO>(`${this.baseUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

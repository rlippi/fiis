import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ProventoResponseDTO } from '../models/dto/provento-response.dto';
import { ProventoRequestVO } from '../models/vo/provento-request.vo';

@Injectable({ providedIn: 'root' })
export class ProventoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/proventos`;

  listar(filtros?: { fundoId?: number; inicio?: string; fim?: string }): Observable<ProventoResponseDTO[]> {
    let params = new HttpParams();
    if (filtros?.fundoId != null) {
      params = params.set('fundoId', filtros.fundoId);
    }
    if (filtros?.inicio) {
      params = params.set('inicio', filtros.inicio);
    }
    if (filtros?.fim) {
      params = params.set('fim', filtros.fim);
    }
    return this.http.get<ProventoResponseDTO[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<ProventoResponseDTO> {
    return this.http.get<ProventoResponseDTO>(`${this.baseUrl}/${id}`);
  }

  criar(request: ProventoRequestVO): Observable<ProventoResponseDTO> {
    return this.http.post<ProventoResponseDTO>(this.baseUrl, request);
  }

  atualizar(id: number, request: ProventoRequestVO): Observable<ProventoResponseDTO> {
    return this.http.put<ProventoResponseDTO>(`${this.baseUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

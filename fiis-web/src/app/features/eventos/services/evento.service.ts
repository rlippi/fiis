import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { EventoCorporativoResponseDTO } from '../models/dto/evento-response.dto';
import { EventoCorporativoRequestVO } from '../models/vo/evento-request.vo';

@Injectable({ providedIn: 'root' })
export class EventoCorporativoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/eventos-corporativos`;

  listar(fundoId?: number): Observable<EventoCorporativoResponseDTO[]> {
    let params = new HttpParams();
    if (fundoId != null) {
      params = params.set('fundoId', fundoId);
    }
    return this.http.get<EventoCorporativoResponseDTO[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<EventoCorporativoResponseDTO> {
    return this.http.get<EventoCorporativoResponseDTO>(`${this.baseUrl}/${id}`);
  }

  criar(request: EventoCorporativoRequestVO): Observable<EventoCorporativoResponseDTO> {
    return this.http.post<EventoCorporativoResponseDTO>(this.baseUrl, request);
  }

  atualizar(id: number, request: EventoCorporativoRequestVO): Observable<EventoCorporativoResponseDTO> {
    return this.http.put<EventoCorporativoResponseDTO>(`${this.baseUrl}/${id}`, request);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

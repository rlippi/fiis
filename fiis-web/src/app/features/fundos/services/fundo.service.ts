import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { FundoResponseDTO } from '../models/dto/fundo-response.dto';
import { FundoRequestVO } from '../models/vo/fundo-request.vo';

@Injectable({ providedIn: 'root' })
export class FundoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/fundos`;

  listar(apenasAtivos = false): Observable<FundoResponseDTO[]> {
    const params = new HttpParams().set('apenasAtivos', apenasAtivos);
    return this.http.get<FundoResponseDTO[]>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<FundoResponseDTO> {
    return this.http.get<FundoResponseDTO>(`${this.baseUrl}/${id}`);
  }

  criar(request: FundoRequestVO): Observable<FundoResponseDTO> {
    return this.http.post<FundoResponseDTO>(this.baseUrl, request);
  }

  atualizar(id: number, request: FundoRequestVO): Observable<FundoResponseDTO> {
    return this.http.put<FundoResponseDTO>(`${this.baseUrl}/${id}`, request);
  }

  desativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

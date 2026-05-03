import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { FundoResumoDTO } from '../models/dto/fundo-resumo.dto';

/**
 * Serviço utilitario para features que precisam listar fundos (dropdown)
 * sem acoplar-se a feature/fundos. Retorna apenas dados resumidos
 * (id, ticker, nome) aproveitando structural typing do TypeScript.
 */
@Injectable({ providedIn: 'root' })
export class FundoLookupService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/fundos`;

  listarResumo(): Observable<FundoResumoDTO[]> {
    return this.http.get<FundoResumoDTO[]>(this.baseUrl);
  }
}

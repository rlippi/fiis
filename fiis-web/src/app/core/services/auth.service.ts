import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DOCUMENT } from '@angular/common';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CredencialVO } from '../models/vo/credencial.vo';
import { TokenResponse } from '../models/dto/token-response.dto';
import { Perfil } from '../models/enumeration/perfil.enum';
import { TokenService } from './token.service';

export interface UsuarioLogado {
  nome: string;
  perfil: Perfil;
}

const USER_KEY = 'fiis-user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);
  private readonly document = inject(DOCUMENT);

  private readonly _currentUser = signal<UsuarioLogado | null>(this.readStoredUser());

  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(
    () => this._currentUser() !== null && this.tokenService.getToken() !== null
  );

  login(credencial: CredencialVO): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${environment.apiUrl}/auth/login`, credencial)
      .pipe(
        tap((response) => {
          this.tokenService.setToken(response.token);
          const user: UsuarioLogado = {
            nome: response.nome,
            perfil: response.perfil
          };
          this.storage?.setItem(USER_KEY, JSON.stringify(user));
          this._currentUser.set(user);
        })
      );
  }

  logout(): void {
    this.tokenService.clearToken();
    this.storage?.removeItem(USER_KEY);
    this._currentUser.set(null);
  }

  private get storage(): Storage | undefined {
    return this.document.defaultView?.localStorage;
  }

  private readStoredUser(): UsuarioLogado | null {
    const raw = this.storage?.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UsuarioLogado;
    } catch {
      return null;
    }
  }
}

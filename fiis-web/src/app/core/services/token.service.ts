import { Injectable, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

const TOKEN_KEY = 'fiis-token';
const REFRESH_TOKEN_KEY = 'fiis-refresh-token';

@Injectable({ providedIn: 'root' })
export class TokenService {
  private readonly document = inject(DOCUMENT);

  private get storage(): Storage | undefined {
    return this.document.defaultView?.localStorage;
  }

  getToken(): string | null {
    return this.storage?.getItem(TOKEN_KEY) ?? null;
  }

  setToken(token: string): void {
    this.storage?.setItem(TOKEN_KEY, token);
  }

  clearToken(): void {
    this.storage?.removeItem(TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return this.storage?.getItem(REFRESH_TOKEN_KEY) ?? null;
  }

  setRefreshToken(token: string): void {
    this.storage?.setItem(REFRESH_TOKEN_KEY, token);
  }

  clearRefreshToken(): void {
    this.storage?.removeItem(REFRESH_TOKEN_KEY);
  }

  /**
   * Limpa ambos os tokens em uma operação. Usado no logout para garantir que
   * nenhum vestígio fique no localStorage.
   */
  clearAll(): void {
    this.clearToken();
    this.clearRefreshToken();
  }
}

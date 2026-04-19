import { Injectable, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

const TOKEN_KEY = 'fiis-token';

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
}

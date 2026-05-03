import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { Mock, vi } from 'vitest';
import { of } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { TokenResponse } from '../../../core/models/dto/token-response.dto';

describe('LoginComponent', () => {
  let authLogin: Mock;
  let routerNavigate: Mock;

  const fakeToken: TokenResponse = {
    token: 't',
    tipo: 'Bearer',
    refreshToken: 'r',
    nome: 'X',
    perfil: 'USER',
    expiraEmMs: 900000
  };

  beforeEach(async () => {
    // jsdom não implementa matchMedia — ThemeService o usa para detectar
    // a preferência light/dark do sistema operacional.
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        addListener: vi.fn(),
        removeListener: vi.fn(),
        dispatchEvent: vi.fn()
      }))
    });

    authLogin = vi.fn().mockReturnValue(of(fakeToken));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: { login: authLogin } }
      ]
    }).compileComponents();

    routerNavigate = vi.spyOn(TestBed.inject(Router), 'navigate') as unknown as Mock;
    routerNavigate.mockResolvedValue(true);
  });

  it('renderiza form de login com campos de email e senha', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const html = fixture.nativeElement as HTMLElement;
    expect(html.querySelector('input[formControlName="email"]')).not.toBeNull();
    // p-password (PrimeNG) renderiza como custom element, não <input> direto
    expect(html.querySelector('p-password[formControlName="senha"]')).not.toBeNull();
  });

  it('renderiza botão "Acessar conta demo" para usuário sem cadastro', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const html = fixture.nativeElement.textContent as string;
    expect(html).toContain('Acessar conta demo');
    expect(html).toContain('Sem cadastro');
  });

  it('submit válido chama AuthService.login e navega para /home', async () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.componentInstance['form'].setValue({ email: 'a@b.com', senha: 'pass1234' });

    fixture.componentInstance['submit']();
    await fixture.whenStable();

    expect(authLogin).toHaveBeenCalledWith({ email: 'a@b.com', senha: 'pass1234' });
    expect(routerNavigate).toHaveBeenCalledWith(['/home']);
  });

  it('botão demo chama login com credenciais hardcoded', async () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.componentInstance['loginComoDemo']();
    await fixture.whenStable();

    expect(authLogin).toHaveBeenCalledWith({ email: 'demo@fiis.com', senha: 'demo1234' });
  });
});

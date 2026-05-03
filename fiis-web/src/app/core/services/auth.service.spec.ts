import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { TokenService } from './token.service';
import { TokenResponse } from '../models/dto/token-response.dto';

const fakeTokenResponse: TokenResponse = {
  token: 'access-jwt-aaa',
  tipo: 'Bearer',
  refreshToken: 'refresh-bbb',
  nome: 'Usuário Teste',
  perfil: 'USER',
  expiraEmMs: 900000
};

describe('AuthService', () => {
  let service: AuthService;
  let tokenService: TokenService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    tokenService = TestBed.inject(TokenService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('login persiste tokens e atualiza currentUser', () => {
    service.login({ email: 'a@b.com', senha: 'pass1234' }).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/api/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'a@b.com', senha: 'pass1234' });
    req.flush(fakeTokenResponse);

    expect(tokenService.getToken()).toBe('access-jwt-aaa');
    expect(tokenService.getRefreshToken()).toBe('refresh-bbb');
    expect(service.currentUser()).toEqual({ nome: 'Usuário Teste', perfil: 'USER' });
    expect(service.isAuthenticated()).toBe(true);
  });

  it('signup persiste tokens da mesma forma que login', () => {
    service
      .signup({ nome: 'Usuário Teste', email: 'a@b.com', senha: 'pass1234' })
      .subscribe();

    const req = http.expectOne(`${environment.apiUrl}/api/auth/signup`);
    expect(req.request.method).toBe('POST');
    req.flush(fakeTokenResponse);

    expect(service.currentUser()?.nome).toBe('Usuário Teste');
    expect(tokenService.getToken()).toBe('access-jwt-aaa');
  });

  it('refresh sem token persistido falha imediatamente sem chamar API', async () => {
    const erro = await new Promise<Error>((resolve) => {
      service.refresh().subscribe({
        error: (e: Error) => resolve(e)
      });
    });

    expect(erro.message).toContain('refresh');
    http.expectNone(`${environment.apiUrl}/api/auth/refresh`);
  });

  it('refresh com token persistido rotaciona o par e atualiza storage', () => {
    tokenService.setRefreshToken('refresh-antigo');

    service.refresh().subscribe();

    const req = http.expectOne(`${environment.apiUrl}/api/auth/refresh`);
    expect(req.request.body).toEqual({ refreshToken: 'refresh-antigo' });
    req.flush(fakeTokenResponse);

    expect(tokenService.getToken()).toBe('access-jwt-aaa');
    expect(tokenService.getRefreshToken()).toBe('refresh-bbb');
  });

  it('logout limpa estado mesmo se a chamada de revogação falhar', () => {
    tokenService.setToken('algum-access');
    tokenService.setRefreshToken('algum-refresh');
    localStorage.setItem('fiis-user', JSON.stringify({ nome: 'X', perfil: 'USER' }));

    service.logout().subscribe();

    const req = http.expectOne(`${environment.apiUrl}/api/auth/logout`);
    req.error(new ProgressEvent('network error'));

    expect(tokenService.getToken()).toBeNull();
    expect(tokenService.getRefreshToken()).toBeNull();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('logout sem refresh token limpa local sem chamar API', () => {
    localStorage.setItem('fiis-user', JSON.stringify({ nome: 'X', perfil: 'USER' }));

    service.logout().subscribe();

    http.expectNone(`${environment.apiUrl}/api/auth/logout`);
    expect(service.currentUser()).toBeNull();
  });
});

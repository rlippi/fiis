import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

/**
 * Intercepta respostas 401 em requisições autenticadas, forçando logout e
 * redirecionamento para a tela de login. Evita que o usuário fique preso em
 * uma tela quebrada após expiração do token.
 *
 * A rota de login é ignorada aqui: um 401 no POST /api/auth/login significa
 * credenciais inválidas, e o próprio LoginComponent trata esse caso.
 */
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const isLoginRequest = req.url.includes('/api/auth/login');

      if (err.status === 401 && !isLoginRequest) {
        authService.logout();
        router.navigate(['/login']);
      }

      return throwError(() => err);
    })
  );
};

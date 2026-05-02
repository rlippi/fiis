import { HttpErrorResponse, HttpEvent, HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

/**
 * Endpoints onde um 401 é "credencial inválida" e NÃO deve disparar refresh —
 * cair em loop tentando refrescar nesses endpoints só pioraria a situação.
 */
const ENDPOINTS_QUE_IGNORAM_REFRESH = [
  '/api/auth/login',
  '/api/auth/signup',
  '/api/auth/refresh',
  '/api/auth/logout'
];

/**
 * Refresh em curso compartilhado entre requisições paralelas.
 * Sem isso, se 5 requisições simultâneas dispararem 401, dispararíamos 5
 * refreshes — o segundo em diante falharia (reuse detection no backend!) e
 * todas as sessões seriam revogadas.
 *
 * O subject emite `true` enquanto está rotacionando, `false` quando terminou.
 * Requisições que chegam durante a rotação aguardam o valor virar `false` e
 * usam o token já atualizado pelo {@link AuthService#refresh()}.
 */
const refrescando$ = new BehaviorSubject<boolean>(false);

/**
 * Intercepta 401 em requisições autenticadas e tenta rotacionar o refresh
 * token automaticamente. Se o refresh tiver sucesso, refaz a requisição
 * original com o novo access JWT. Se falhar (refresh expirado, revogado,
 * inexistente), faz logout local e redireciona para a tela de login.
 */
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const ignorar = ENDPOINTS_QUE_IGNORAM_REFRESH.some((rota) => req.url.includes(rota));
      if (err.status !== 401 || ignorar) {
        return throwError(() => err);
      }

      return tentarRefreshERetentar(req, next, authService, tokenService, router, err);
    })
  );
};

function tentarRefreshERetentar(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
  tokenService: TokenService,
  router: Router,
  erroOriginal: HttpErrorResponse
): Observable<HttpEvent<unknown>> {
  // Se já há um refresh em curso, aguarda terminar e refaz a request com o
  // novo token (atualizado pelo refresh em andamento).
  if (refrescando$.value) {
    return refrescando$.pipe(
      filter((emCurso) => !emCurso),
      take(1),
      switchMap(() => next(reqComTokenAtualizado(req, tokenService)))
    );
  }

  refrescando$.next(true);
  return authService.refresh().pipe(
    switchMap(() => {
      refrescando$.next(false);
      return next(reqComTokenAtualizado(req, tokenService));
    }),
    catchError(() => {
      refrescando$.next(false);
      // Refresh falhou (token revogado/expirado/inexistente). Limpa local
      // e manda para login. Mantém o erro original — o caller pode tratar.
      authService.logout().subscribe({ complete: () => router.navigate(['/login']) });
      return throwError(() => erroOriginal);
    })
  );
}

/**
 * Clona a request injetando o token JWT atual no header. Necessário porque o
 * {@code jwtInterceptor} já passou nesta cadeia — refazer {@code next(req)}
 * sem clonar usaria o token antigo (anexado no original).
 */
function reqComTokenAtualizado(
  req: HttpRequest<unknown>,
  tokenService: TokenService
): HttpRequest<unknown> {
  const token = tokenService.getToken();
  if (!token) {
    return req;
  }
  return req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  });
}

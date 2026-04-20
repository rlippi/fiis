import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { TokenService } from '../services/token.service';

const ROTAS_PUBLICAS = ['/auth/login'];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(TokenService).getToken();
  const isPublic = ROTAS_PUBLICAS.some((rota) => req.url.includes(rota));

  if (!token || isPublic) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authReq);
};

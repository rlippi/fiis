import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      )
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/layout/app-shell/app-shell.component').then(
        (m) => m.AppShellComponent
      ),
    children: [
      {
        path: 'home',
        loadComponent: () =>
          import('./features/dashboard/home/home.component').then(
            (m) => m.HomeComponent
          )
      },
      {
        path: 'fundos',
        loadChildren: () =>
          import('./features/fundos/fundos.routes').then((m) => m.fundosRoutes)
      },
      {
        path: 'operacoes',
        loadChildren: () =>
          import('./features/operacoes/operacoes.routes').then(
            (m) => m.operacoesRoutes
          )
      },
      {
        path: 'proventos',
        loadChildren: () =>
          import('./features/proventos/proventos.routes').then(
            (m) => m.proventosRoutes
          )
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];

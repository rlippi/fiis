import { Routes } from '@angular/router';

export const fundosRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/fundo-list/fundo-list.component').then(
        (m) => m.FundoListComponent
      )
  },
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/fundo-form/fundo-form.component').then(
        (m) => m.FundoFormComponent
      )
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/fundo-form/fundo-form.component').then(
        (m) => m.FundoFormComponent
      )
  }
];

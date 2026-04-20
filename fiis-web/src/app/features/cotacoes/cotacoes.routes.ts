import { Routes } from '@angular/router';

export const cotacoesRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/cotacao-list/cotacao-list.component').then(
        (m) => m.CotacaoListComponent
      )
  },
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/cotacao-form/cotacao-form.component').then(
        (m) => m.CotacaoFormComponent
      )
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/cotacao-form/cotacao-form.component').then(
        (m) => m.CotacaoFormComponent
      )
  }
];

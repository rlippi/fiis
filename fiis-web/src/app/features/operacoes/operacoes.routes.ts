import { Routes } from '@angular/router';

export const operacoesRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/operacao-list/operacao-list.component').then(
        (m) => m.OperacaoListComponent
      )
  },
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/operacao-form/operacao-form.component').then(
        (m) => m.OperacaoFormComponent
      )
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/operacao-form/operacao-form.component').then(
        (m) => m.OperacaoFormComponent
      )
  }
];

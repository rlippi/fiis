import { Routes } from '@angular/router';

export const relatoriosRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/relatorios/relatorios.component').then(
        (m) => m.RelatoriosComponent
      )
  }
];

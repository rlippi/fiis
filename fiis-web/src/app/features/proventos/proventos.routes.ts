import { Routes } from '@angular/router';

export const proventosRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/provento-list/provento-list.component').then(
        (m) => m.ProventoListComponent
      )
  },
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/provento-form/provento-form.component').then(
        (m) => m.ProventoFormComponent
      )
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/provento-form/provento-form.component').then(
        (m) => m.ProventoFormComponent
      )
  }
];

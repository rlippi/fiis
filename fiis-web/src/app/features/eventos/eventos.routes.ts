import { Routes } from '@angular/router';

export const eventosRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/evento-list/evento-list.component').then(
        (m) => m.EventoListComponent
      )
  },
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/evento-form/evento-form.component').then(
        (m) => m.EventoFormComponent
      )
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/evento-form/evento-form.component').then(
        (m) => m.EventoFormComponent
      )
  }
];

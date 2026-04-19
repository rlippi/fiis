import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { SelectButtonModule } from 'primeng/selectbutton';

import { ThemeService, ThemeMode } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    FormsModule,
    ButtonModule,
    CardModule,
    SelectButtonModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly themeService = inject(ThemeService);

  protected readonly themeMode = this.themeService.mode;

  protected readonly themeOptions: { label: string; value: ThemeMode }[] = [
    { label: 'Sistema', value: 'system' },
    { label: 'Claro', value: 'light' },
    { label: 'Escuro', value: 'dark' }
  ];

  setTheme(mode: ThemeMode): void {
    this.themeService.setMode(mode);
  }
}

import { Injectable, signal, effect, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

export type ThemeMode = 'system' | 'light' | 'dark';

const STORAGE_KEY = 'fiis-theme';
const DARK_CLASS = 'app-dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);

  readonly mode = signal<ThemeMode>(this.readStoredMode());

  private readonly systemDarkMedia =
    this.document.defaultView?.matchMedia('(prefers-color-scheme: dark)');

  constructor() {
    effect(() => {
      this.applyTheme(this.mode());
    });

    this.systemDarkMedia?.addEventListener('change', () => {
      if (this.mode() === 'system') {
        this.applyTheme('system');
      }
    });
  }

  setMode(mode: ThemeMode): void {
    this.mode.set(mode);
    localStorage.setItem(STORAGE_KEY, mode);
  }

  private readStoredMode(): ThemeMode {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark' || stored === 'system') {
      return stored;
    }
    return 'system';
  }

  private applyTheme(mode: ThemeMode): void {
    const isDark =
      mode === 'dark' ||
      (mode === 'system' && !!this.systemDarkMedia?.matches);

    const html = this.document.documentElement;
    if (isDark) {
      html.classList.add(DARK_CLASS);
    } else {
      html.classList.remove(DARK_CLASS);
    }
  }
}

import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';

import { AuthService } from '../../../core/services/auth.service';
import { ErrorService } from '../../../core/services/error.service';
import { ThemeMode, ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-esqueci-senha',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    InputTextModule,
    MessageModule
  ],
  templateUrl: './esqueci-senha.component.html',
  styleUrl: './esqueci-senha.component.scss'
})
export class EsqueciSenhaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly errorService = inject(ErrorService);
  private readonly themeService = inject(ThemeService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]]
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly enviado = signal(false);
  protected readonly themeMode = this.themeService.mode;

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.forgotPassword(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.enviado.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.errorService.mapMessage(err));
      }
    });
  }

  cycleTheme(): void {
    const modes: ThemeMode[] = ['system', 'light', 'dark'];
    const current = this.themeMode();
    const next = modes[(modes.indexOf(current) + 1) % modes.length];
    this.themeService.setMode(next);
  }

  get themeIcon(): string {
    switch (this.themeMode()) {
      case 'light': return 'pi pi-sun';
      case 'dark': return 'pi pi-moon';
      default: return 'pi pi-desktop';
    }
  }
}

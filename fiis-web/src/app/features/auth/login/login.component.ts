import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';

import { AuthService } from '../../../core/services/auth.service';
import { ThemeMode, ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    MessageModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly themeService = inject(ThemeService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required]
  });

  protected readonly loading = signal(false);
  protected readonly loadingDemo = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly themeMode = this.themeService.mode;

  private readonly DEMO_EMAIL = 'demo@fiis.com';
  private readonly DEMO_SENHA = 'demo1234';

  submit(): void {
    if (this.form.invalid || this.loading()) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/home']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(this.mapError(err));
      }
    });
  }

  loginComoDemo(): void {
    if (this.loadingDemo() || this.loading()) {
      return;
    }

    this.loadingDemo.set(true);
    this.errorMessage.set(null);

    this.auth.login({ email: this.DEMO_EMAIL, senha: this.DEMO_SENHA }).subscribe({
      next: () => {
        this.router.navigate(['/home']);
      },
      error: (err: HttpErrorResponse) => {
        this.loadingDemo.set(false);
        this.errorMessage.set(this.mapDemoError(err));
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
      case 'light':
        return 'pi pi-sun';
      case 'dark':
        return 'pi pi-moon';
      default:
        return 'pi pi-desktop';
    }
  }

  private mapError(err: HttpErrorResponse): string {
    if (err.status === 401) {
      return 'E-mail ou senha invalidos.';
    }
    if (err.status === 429) {
      return err.error?.mensagem
        ?? 'Muitas tentativas em pouco tempo. Aguarde alguns minutos e tente novamente.';
    }
    if (err.status === 0) {
      return 'Nao foi possivel conectar a API. Verifique sua conexao.';
    }
    return 'Erro ao efetuar login. Tente novamente.';
  }

  private mapDemoError(err: HttpErrorResponse): string {
    if (err.status === 401) {
      return 'Conta demo não disponível neste ambiente.';
    }
    return this.mapError(err);
  }
}

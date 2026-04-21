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
import { ErrorService } from '../../../core/services/error.service';
import { ThemeMode, ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-signup',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    MessageModule
  ],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss'
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);
  private readonly themeService = inject(ThemeService);

  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    senha: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(50)]]
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly themeMode = this.themeService.mode;

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.auth.signup(this.form.getRawValue()).subscribe({
      next: () => { this.router.navigate(['/home']); },
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

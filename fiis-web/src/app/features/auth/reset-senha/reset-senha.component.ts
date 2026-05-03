import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';

import { AuthService } from '../../../core/services/auth.service';
import { ErrorService } from '../../../core/services/error.service';
import { ThemeMode, ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-reset-senha',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    MessageModule,
    PasswordModule
  ],
  templateUrl: './reset-senha.component.html',
  styleUrl: './reset-senha.component.scss'
})
export class ResetSenhaComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly errorService = inject(ErrorService);
  private readonly themeService = inject(ThemeService);

  protected readonly form = this.fb.nonNullable.group({
    novaSenha: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(50),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)
      ]
    ],
    confirmacao: ['', [Validators.required]]
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly concluido = signal(false);
  protected readonly tokenAusente = signal(false);
  protected readonly themeMode = this.themeService.mode;

  private token = '';

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.tokenAusente.set(true);
    }
  }

  submit(): void {
    if (this.form.invalid || this.loading() || !this.token) return;

    const { novaSenha, confirmacao } = this.form.getRawValue();
    if (novaSenha !== confirmacao) {
      this.errorMessage.set('A confirmação não confere com a nova senha.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.resetPassword({ token: this.token, novaSenha }).subscribe({
      next: () => {
        this.loading.set(false);
        this.concluido.set(true);
        setTimeout(() => this.router.navigate(['/login']), 3500);
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

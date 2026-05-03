import { Component, OnInit, computed, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { ToggleSwitchModule } from 'primeng/toggleswitch';

import { ErrorService } from '../../../../core/services/error.service';
import {
  SEGMENTOS,
  Segmento,
  SegmentoOption
} from '../../models/enumeration/segmento.enum';
import {
  TIPOS_FUNDO,
  TipoFundo,
  TipoFundoOption
} from '../../models/enumeration/tipo-fundo.enum';
import { FundoRequestVO } from '../../models/vo/fundo-request.vo';
import { FundoService } from '../../services/fundo.service';

@Component({
  selector: 'app-fundo-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    InputMaskModule,
    InputTextModule,
    MessageModule,
    SelectModule,
    ToggleSwitchModule
  ],
  templateUrl: './fundo-form.component.html',
  styleUrl: './fundo-form.component.scss'
})
export class FundoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fundoService = inject(FundoService);
  private readonly messageService = inject(MessageService);
  private readonly errorService = inject(ErrorService);

  protected readonly tipos: TipoFundoOption[] = TIPOS_FUNDO;
  protected readonly segmentos: SegmentoOption[] = SEGMENTOS;

  protected readonly editingId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);

  protected readonly titulo = computed(() =>
    this.editingId() == null ? 'Novo fundo' : 'Editar fundo'
  );

  protected readonly form = this.fb.nonNullable.group({
    ticker: [
      '',
      [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(10),
        Validators.pattern(/^[A-Z0-9]+$/)
      ]
    ],
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    cnpj: [''],
    tipo: ['' as TipoFundo | '', Validators.required],
    segmento: ['' as Segmento | '', Validators.required],
    ativo: [true]
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.editingId.set(id);
      this.carregar(id);
    }
  }

  private carregar(id: number): void {
    this.loading.set(true);
    this.fundoService.buscarPorId(id).subscribe({
      next: (fundo) => {
        this.form.patchValue({
          ticker: fundo.ticker,
          nome: fundo.nome,
          cnpj: fundo.cnpj ?? '',
          tipo: fundo.tipo,
          segmento: fundo.segmento,
          ativo: fundo.ativo
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar fundo');
        this.router.navigate(['/fundos']);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const request: FundoRequestVO = {
      ticker: value.ticker.toUpperCase(),
      nome: value.nome,
      cnpj: this.limparCnpj(value.cnpj) || null,
      tipo: value.tipo as TipoFundo,
      segmento: value.segmento as Segmento,
      ativo: value.ativo
    };

    this.saving.set(true);
    const id = this.editingId();
    const req$ =
      id == null
        ? this.fundoService.criar(request)
        : this.fundoService.atualizar(id, request);

    req$.subscribe({
      next: (fundo) => {
        this.saving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: id == null ? 'Fundo criado' : 'Fundo atualizado',
          detail: `${fundo.ticker} — ${fundo.nome}`
        });
        this.router.navigate(['/fundos']);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorService.showToast(
          err,
          id == null ? 'Erro ao criar fundo' : 'Erro ao atualizar'
        );
      }
    });
  }

  private limparCnpj(cnpj: string): string {
    return (cnpj ?? '').replace(/\D/g, '');
  }
}

import { formatDate } from '@angular/common';
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
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';

import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';
import { ErrorService } from '../../../../core/services/error.service';
import { FundoLookupService } from '../../../../core/services/fundo-lookup.service';
import {
  TIPOS_OPERACAO,
  TipoOperacao,
  TipoOperacaoOption
} from '../../models/enumeration/tipo-operacao.enum';
import { OperacaoRequestVO } from '../../models/vo/operacao-request.vo';
import { OperacaoService } from '../../services/operacao.service';

@Component({
  selector: 'app-operacao-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    DatePickerModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    SelectModule,
    TextareaModule
  ],
  templateUrl: './operacao-form.component.html',
  styleUrl: './operacao-form.component.scss'
})
export class OperacaoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly operacaoService = inject(OperacaoService);
  private readonly fundoLookup = inject(FundoLookupService);
  private readonly messageService = inject(MessageService);
  private readonly errorService = inject(ErrorService);

  protected readonly tipos: TipoOperacaoOption[] = TIPOS_OPERACAO;
  protected readonly hoje = new Date();

  protected readonly editingId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly fundos = signal<FundoResumoDTO[]>([]);

  protected readonly titulo = computed(() =>
    this.editingId() == null ? 'Nova operação' : 'Editar operação'
  );

  protected readonly form = this.fb.nonNullable.group({
    fundoId: [null as number | null, Validators.required],
    tipo: ['' as TipoOperacao | '', Validators.required],
    dataOperacao: [null as Date | null, Validators.required],
    quantidade: [null as number | null, [Validators.required, Validators.min(1)]],
    precoUnitario: [null as number | null, [Validators.required, Validators.min(0.01)]],
    taxas: [0 as number | null],
    observacao: ['']
  });

  ngOnInit(): void {
    this.loading.set(true);
    this.fundoLookup.listarResumo().subscribe({
      next: (fundos) => {
        this.fundos.set(fundos);
        const idParam = this.route.snapshot.paramMap.get('id');
        if (idParam) {
          const id = Number(idParam);
          this.editingId.set(id);
          this.carregar(id);
        } else {
          this.loading.set(false);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar fundos');
      }
    });
  }

  private carregar(id: number): void {
    this.operacaoService.buscarPorId(id).subscribe({
      next: (op) => {
        this.form.patchValue({
          fundoId: op.fundo.id,
          tipo: op.tipo,
          dataOperacao: new Date(op.dataOperacao + 'T00:00:00'),
          quantidade: op.quantidade,
          precoUnitario: op.precoUnitario,
          taxas: op.taxas,
          observacao: op.observacao ?? ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar operação');
        this.router.navigate(['/operacoes']);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const request: OperacaoRequestVO = {
      fundoId: v.fundoId!,
      tipo: v.tipo as TipoOperacao,
      dataOperacao: formatDate(v.dataOperacao!, 'yyyy-MM-dd', 'pt-BR'),
      quantidade: v.quantidade!,
      precoUnitario: v.precoUnitario!,
      taxas: v.taxas ?? 0,
      observacao: v.observacao?.trim() || null
    };

    this.saving.set(true);
    const id = this.editingId();
    const req$ =
      id == null
        ? this.operacaoService.criar(request)
        : this.operacaoService.atualizar(id, request);

    req$.subscribe({
      next: (op) => {
        this.saving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: id == null ? 'Operação criada' : 'Operação atualizada',
          detail: `${op.tipoDescricao} de ${op.quantidade} cotas de ${op.fundo.ticker}.`
        });
        this.router.navigate(['/operacoes']);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorService.showToast(
          err,
          id == null ? 'Erro ao criar operação' : 'Erro ao atualizar'
        );
      }
    });
  }
}

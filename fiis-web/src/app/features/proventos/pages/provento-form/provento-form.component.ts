import { HttpErrorResponse } from '@angular/common/http';
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
import { FundoLookupService } from '../../../../core/services/fundo-lookup.service';
import {
  TIPOS_PROVENTO,
  TipoProvento,
  TipoProventoOption
} from '../../models/enumeration/tipo-provento.enum';
import { ProventoRequestVO } from '../../models/vo/provento-request.vo';
import { ProventoService } from '../../services/provento.service';

@Component({
  selector: 'app-provento-form',
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
  templateUrl: './provento-form.component.html',
  styleUrl: './provento-form.component.scss'
})
export class ProventoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly proventoService = inject(ProventoService);
  private readonly fundoLookup = inject(FundoLookupService);
  private readonly messageService = inject(MessageService);

  protected readonly tipos: TipoProventoOption[] = TIPOS_PROVENTO;
  protected readonly hoje = new Date();

  protected readonly editingId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly fundos = signal<FundoResumoDTO[]>([]);

  protected readonly titulo = computed(() =>
    this.editingId() == null ? 'Novo provento' : 'Editar provento'
  );

  protected readonly form = this.fb.nonNullable.group({
    fundoId: [null as number | null, Validators.required],
    tipoProvento: ['' as TipoProvento | '', Validators.required],
    dataReferencia: [null as Date | null, Validators.required],
    dataPagamento: [null as Date | null, Validators.required],
    valorPorCota: [null as number | null, [Validators.required, Validators.min(0.000001)]],
    quantidadeCotas: [null as number | null, [Validators.required, Validators.min(1)]],
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
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao carregar fundos',
          detail: this.mapError(err)
        });
      }
    });
  }

  private carregar(id: number): void {
    this.proventoService.buscarPorId(id).subscribe({
      next: (prov) => {
        this.form.patchValue({
          fundoId: prov.fundo.id,
          tipoProvento: prov.tipoProvento,
          dataReferencia: new Date(prov.dataReferencia + 'T00:00:00'),
          dataPagamento: new Date(prov.dataPagamento + 'T00:00:00'),
          valorPorCota: prov.valorPorCota,
          quantidadeCotas: prov.quantidadeCotas,
          observacao: prov.observacao ?? ''
        });
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao carregar provento',
          detail: this.mapError(err)
        });
        this.router.navigate(['/proventos']);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const request: ProventoRequestVO = {
      fundoId: v.fundoId!,
      tipoProvento: v.tipoProvento as TipoProvento,
      dataReferencia: formatDate(v.dataReferencia!, 'yyyy-MM-dd', 'pt-BR'),
      dataPagamento: formatDate(v.dataPagamento!, 'yyyy-MM-dd', 'pt-BR'),
      valorPorCota: v.valorPorCota!,
      quantidadeCotas: v.quantidadeCotas!,
      observacao: v.observacao?.trim() || null
    };

    this.saving.set(true);
    const id = this.editingId();
    const req$ =
      id == null
        ? this.proventoService.criar(request)
        : this.proventoService.atualizar(id, request);

    req$.subscribe({
      next: (prov) => {
        this.saving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: id == null ? 'Provento criado' : 'Provento atualizado',
          detail: `${prov.tipoProventoDescricao} de ${prov.fundo.ticker}.`
        });
        this.router.navigate(['/proventos']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.messageService.add({
          severity: 'error',
          summary: id == null ? 'Erro ao criar provento' : 'Erro ao atualizar',
          detail: this.mapError(err)
        });
      }
    });
  }

  private mapError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Não foi possível conectar à API.';
    }
    const serverMessage = err.error?.mensagem;
    return serverMessage ?? 'Erro inesperado.';
  }
}

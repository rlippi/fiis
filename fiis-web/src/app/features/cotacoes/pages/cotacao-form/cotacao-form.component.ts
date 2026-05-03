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

import { FundoResumoDTO } from '../../../../core/models/dto/fundo-resumo.dto';
import { ErrorService } from '../../../../core/services/error.service';
import { FundoLookupService } from '../../../../core/services/fundo-lookup.service';
import { CotacaoRequestVO } from '../../models/vo/cotacao-request.vo';
import { CotacaoService } from '../../services/cotacao.service';

@Component({
  selector: 'app-cotacao-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    CardModule,
    DatePickerModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    SelectModule
  ],
  templateUrl: './cotacao-form.component.html',
  styleUrl: './cotacao-form.component.scss'
})
export class CotacaoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cotacaoService = inject(CotacaoService);
  private readonly fundoLookup = inject(FundoLookupService);
  private readonly messageService = inject(MessageService);
  private readonly errorService = inject(ErrorService);

  protected readonly hoje = new Date();

  protected readonly editingId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly fundos = signal<FundoResumoDTO[]>([]);

  protected readonly titulo = computed(() =>
    this.editingId() == null ? 'Nova cotação' : 'Editar cotação'
  );

  protected readonly form = this.fb.nonNullable.group({
    fundoId: [null as number | null, Validators.required],
    data: [null as Date | null, Validators.required],
    precoFechamento: [null as number | null, [Validators.required, Validators.min(0.0001)]],
    precoAbertura: [null as number | null],
    precoMinimo: [null as number | null],
    precoMaximo: [null as number | null],
    volume: [null as number | null]
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
    this.cotacaoService.buscarPorId(id).subscribe({
      next: (cot) => {
        this.form.patchValue({
          fundoId: cot.fundo.id,
          data: new Date(cot.data + 'T00:00:00'),
          precoFechamento: cot.precoFechamento,
          precoAbertura: cot.precoAbertura,
          precoMinimo: cot.precoMinimo,
          precoMaximo: cot.precoMaximo,
          volume: cot.volume
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar cotação');
        this.router.navigate(['/cotacoes']);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const request: CotacaoRequestVO = {
      fundoId: v.fundoId!,
      data: formatDate(v.data!, 'yyyy-MM-dd', 'pt-BR'),
      precoFechamento: v.precoFechamento!,
      precoAbertura: v.precoAbertura,
      precoMinimo: v.precoMinimo,
      precoMaximo: v.precoMaximo,
      volume: v.volume
    };

    this.saving.set(true);
    const id = this.editingId();
    const req$ =
      id == null
        ? this.cotacaoService.criar(request)
        : this.cotacaoService.atualizar(id, request);

    req$.subscribe({
      next: (cot) => {
        this.saving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: id == null ? 'Cotação criada' : 'Cotação atualizada',
          detail: `${cot.fundo.ticker} em ${cot.data.split('-').reverse().join('/')}.`
        });
        this.router.navigate(['/cotacoes']);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorService.showToast(
          err,
          id == null ? 'Erro ao criar cotação' : 'Erro ao atualizar'
        );
      }
    });
  }
}

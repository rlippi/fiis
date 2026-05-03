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
  TIPOS_EVENTO_CORPORATIVO,
  TipoEventoCorporativo,
  TipoEventoCorporativoOption
} from '../../models/enumeration/tipo-evento-corporativo.enum';
import { EventoCorporativoRequestVO } from '../../models/vo/evento-request.vo';
import { EventoCorporativoService } from '../../services/evento.service';

@Component({
  selector: 'app-evento-form',
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
  templateUrl: './evento-form.component.html',
  styleUrl: './evento-form.component.scss'
})
export class EventoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly eventoService = inject(EventoCorporativoService);
  private readonly fundoLookup = inject(FundoLookupService);
  private readonly messageService = inject(MessageService);
  private readonly errorService = inject(ErrorService);

  protected readonly tipos: TipoEventoCorporativoOption[] = TIPOS_EVENTO_CORPORATIVO;
  protected readonly hoje = new Date();

  protected readonly editingId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly fundos = signal<FundoResumoDTO[]>([]);

  protected readonly titulo = computed(() =>
    this.editingId() == null ? 'Novo evento corporativo' : 'Editar evento corporativo'
  );

  protected readonly form = this.fb.nonNullable.group({
    fundoId: [null as number | null, Validators.required],
    tipo: ['' as TipoEventoCorporativo | '', Validators.required],
    data: [null as Date | null, Validators.required],
    fator: [null as number | null, [Validators.required, Validators.min(0.000001)]],
    descricao: ['']
  });

  protected readonly tipoSelecionado = computed(() => {
    const tipo = this.form.controls.tipo.value;
    return this.tipos.find((t) => t.value === tipo) ?? null;
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
    this.eventoService.buscarPorId(id).subscribe({
      next: (ev) => {
        this.form.patchValue({
          fundoId: ev.fundo.id,
          tipo: ev.tipo,
          data: new Date(ev.data + 'T00:00:00'),
          fator: ev.fator,
          descricao: ev.descricao ?? ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar evento');
        this.router.navigate(['/eventos']);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const request: EventoCorporativoRequestVO = {
      fundoId: v.fundoId!,
      tipo: v.tipo as TipoEventoCorporativo,
      data: formatDate(v.data!, 'yyyy-MM-dd', 'pt-BR'),
      fator: v.fator!,
      descricao: v.descricao?.trim() || null
    };

    this.saving.set(true);
    const id = this.editingId();
    const req$ =
      id == null
        ? this.eventoService.criar(request)
        : this.eventoService.atualizar(id, request);

    req$.subscribe({
      next: (ev) => {
        this.saving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: id == null ? 'Evento criado' : 'Evento atualizado',
          detail: `${ev.tipoDescricao} de ${ev.fundo.ticker}.`
        });
        this.router.navigate(['/eventos']);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorService.showToast(
          err,
          id == null ? 'Erro ao criar evento' : 'Erro ao atualizar'
        );
      }
    });
  }
}

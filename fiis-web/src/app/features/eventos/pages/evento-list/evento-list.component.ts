import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';

import { ErrorService } from '../../../../core/services/error.service';
import { EventoCorporativoResponseDTO } from '../../models/dto/evento-response.dto';
import { EventoCorporativoService } from '../../services/evento.service';

@Component({
  selector: 'app-evento-list',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    RouterLink,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    TableModule,
    TagModule,
    ToolbarModule
  ],
  templateUrl: './evento-list.component.html',
  styleUrl: './evento-list.component.scss'
})
export class EventoListComponent implements OnInit {
  private readonly eventoService = inject(EventoCorporativoService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly errorService = inject(ErrorService);

  protected readonly eventos = signal<EventoCorporativoResponseDTO[]>([]);
  protected readonly loading = signal(false);
  protected readonly globalFilter = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.eventoService.listar().subscribe({
      next: (lista) => {
        this.eventos.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar eventos');
      }
    });
  }

  editar(ev: EventoCorporativoResponseDTO): void {
    this.router.navigate(['/eventos', ev.id, 'editar']);
  }

  confirmarExclusao(ev: EventoCorporativoResponseDTO): void {
    this.confirmationService.confirm({
      header: 'Excluir evento',
      message: `Tem certeza que deseja excluir este ${ev.tipoDescricao.toLowerCase()} de ${ev.fundo.ticker}? Esta ação não pode ser desfeita.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deletar(ev)
    });
  }

  tagSeverity(tipo: string): 'info' | 'warn' | 'danger' {
    if (tipo === 'BONIFICACAO') return 'info';
    if (tipo === 'DESDOBRAMENTO') return 'warn';
    return 'danger';
  }

  private deletar(ev: EventoCorporativoResponseDTO): void {
    this.eventoService.deletar(ev.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Evento excluído',
          detail: `${ev.tipoDescricao} de ${ev.fundo.ticker} removido.`
        });
        this.carregar();
      },
      error: (err) => {
        this.errorService.showToast(err, 'Erro ao excluir');
      }
    });
  }
}

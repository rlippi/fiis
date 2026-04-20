import { HttpErrorResponse } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';
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

import { ProventoResponseDTO } from '../../models/dto/provento-response.dto';
import { ProventoService } from '../../services/provento.service';

@Component({
  selector: 'app-provento-list',
  imports: [
    CurrencyPipe,
    DatePipe,
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
  templateUrl: './provento-list.component.html',
  styleUrl: './provento-list.component.scss'
})
export class ProventoListComponent implements OnInit {
  private readonly proventoService = inject(ProventoService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  protected readonly proventos = signal<ProventoResponseDTO[]>([]);
  protected readonly loading = signal(false);
  protected readonly globalFilter = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.proventoService.listar().subscribe({
      next: (lista) => {
        this.proventos.set(lista);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao carregar proventos',
          detail: this.mapError(err)
        });
      }
    });
  }

  editar(prov: ProventoResponseDTO): void {
    this.router.navigate(['/proventos', prov.id, 'editar']);
  }

  confirmarExclusao(prov: ProventoResponseDTO): void {
    this.confirmationService.confirm({
      header: 'Excluir provento',
      message: `Tem certeza que deseja excluir este ${prov.tipoProventoDescricao.toLowerCase()} de ${prov.fundo.ticker}? Esta ação não pode ser desfeita.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deletar(prov)
    });
  }

  private deletar(prov: ProventoResponseDTO): void {
    this.proventoService.deletar(prov.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Provento excluído',
          detail: `${prov.tipoProventoDescricao} de ${prov.fundo.ticker} removido.`
        });
        this.carregar();
      },
      error: (err: HttpErrorResponse) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao excluir',
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

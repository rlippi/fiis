import { HttpErrorResponse } from '@angular/common/http';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ToolbarModule } from 'primeng/toolbar';

import { CotacaoResponseDTO } from '../../models/dto/cotacao-response.dto';
import { CotacaoService } from '../../services/cotacao.service';

@Component({
  selector: 'app-cotacao-list',
  imports: [
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    FormsModule,
    RouterLink,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    TableModule,
    ToolbarModule
  ],
  templateUrl: './cotacao-list.component.html',
  styleUrl: './cotacao-list.component.scss'
})
export class CotacaoListComponent implements OnInit {
  private readonly cotacaoService = inject(CotacaoService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  protected readonly cotacoes = signal<CotacaoResponseDTO[]>([]);
  protected readonly loading = signal(false);
  protected readonly globalFilter = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.cotacaoService.listar().subscribe({
      next: (lista) => {
        this.cotacoes.set(lista);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao carregar cotações',
          detail: this.mapError(err)
        });
      }
    });
  }

  editar(cot: CotacaoResponseDTO): void {
    this.router.navigate(['/cotacoes', cot.id, 'editar']);
  }

  confirmarExclusao(cot: CotacaoResponseDTO): void {
    this.confirmationService.confirm({
      header: 'Excluir cotação',
      message: `Tem certeza que deseja excluir a cotação de ${cot.fundo.ticker} em ${this.formatarData(cot.data)}? Esta ação não pode ser desfeita.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deletar(cot)
    });
  }

  private deletar(cot: CotacaoResponseDTO): void {
    this.cotacaoService.deletar(cot.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Cotação excluída',
          detail: `${cot.fundo.ticker} em ${this.formatarData(cot.data)} removida.`
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

  private formatarData(iso: string): string {
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }

  private mapError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Não foi possível conectar à API.';
    }
    const serverMessage = err.error?.mensagem;
    return serverMessage ?? 'Erro inesperado.';
  }
}

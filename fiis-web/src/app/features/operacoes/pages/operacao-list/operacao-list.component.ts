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

import { ErrorService } from '../../../../core/services/error.service';
import { OperacaoResponseDTO } from '../../models/dto/operacao-response.dto';
import { OperacaoService } from '../../services/operacao.service';

@Component({
  selector: 'app-operacao-list',
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
  templateUrl: './operacao-list.component.html',
  styleUrl: './operacao-list.component.scss'
})
export class OperacaoListComponent implements OnInit {
  private readonly operacaoService = inject(OperacaoService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly errorService = inject(ErrorService);

  protected readonly operacoes = signal<OperacaoResponseDTO[]>([]);
  protected readonly loading = signal(false);
  protected readonly globalFilter = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.operacaoService.listar().subscribe({
      next: (lista) => {
        this.operacoes.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar operações');
      }
    });
  }

  editar(op: OperacaoResponseDTO): void {
    this.router.navigate(['/operacoes', op.id, 'editar']);
  }

  confirmarExclusao(op: OperacaoResponseDTO): void {
    this.confirmationService.confirm({
      header: 'Excluir operação',
      message: `Tem certeza que deseja excluir esta ${op.tipoDescricao.toLowerCase()} de ${op.quantidade} cotas de ${op.fundo.ticker}? Esta ação não pode ser desfeita.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Excluir',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deletar(op)
    });
  }

  private deletar(op: OperacaoResponseDTO): void {
    this.operacaoService.deletar(op.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Operação excluída',
          detail: `${op.tipoDescricao} de ${op.fundo.ticker} removida.`
        });
        this.carregar();
      },
      error: (err) => {
        this.errorService.showToast(err, 'Erro ao excluir');
      }
    });
  }
}

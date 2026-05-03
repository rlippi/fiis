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

import { ErrorService } from '../../../../core/services/error.service';
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
  private readonly errorService = inject(ErrorService);

  protected readonly cotacoes = signal<CotacaoResponseDTO[]>([]);
  protected readonly loading = signal(false);
  protected readonly importando = signal(false);
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
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar cotações');
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
      error: (err) => {
        this.errorService.showToast(err, 'Erro ao excluir');
      }
    });
  }

  atualizarCarteira(): void {
    if (this.importando()) {
      return;
    }
    this.importando.set(true);
    this.cotacaoService.importarBrapi().subscribe({
      next: (resumo) => {
        this.importando.set(false);

        const mudancas = resumo.criados + resumo.atualizados;
        if (mudancas === 0 && resumo.totalFundos === 0) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Carteira vazia',
            detail: 'Nenhum fundo ativo encontrado para atualizar.'
          });
          return;
        }

        const detalhePartes: string[] = [];
        if (resumo.criados > 0) {
          detalhePartes.push(`${resumo.criados} criada${resumo.criados > 1 ? 's' : ''}`);
        }
        if (resumo.atualizados > 0) {
          detalhePartes.push(`${resumo.atualizados} atualizada${resumo.atualizados > 1 ? 's' : ''}`);
        }
        const detalhe = detalhePartes.length > 0
          ? `${detalhePartes.join(', ')} de ${resumo.totalFundos} fundo${resumo.totalFundos > 1 ? 's' : ''}.`
          : `${resumo.totalFundos} fundo${resumo.totalFundos > 1 ? 's' : ''} processado${resumo.totalFundos > 1 ? 's' : ''}.`;

        this.messageService.add({
          severity: 'success',
          summary: 'Cotações atualizadas',
          detail: detalhe
        });

        if (resumo.naoEncontradosBrapi.length > 0) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Tickers não encontrados na BRAPI',
            detail: resumo.naoEncontradosBrapi.join(', ')
          });
        }

        this.carregar();
      },
      error: (err) => {
        this.importando.set(false);
        this.errorService.showToast(err, 'Erro ao importar cotações');
      }
    });
  }

  private formatarData(iso: string): string {
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }
}

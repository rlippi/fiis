import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MessageService } from 'primeng/api';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { SkeletonModule } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { forkJoin } from 'rxjs';

import { ErrorService } from '../../../../core/services/error.service';
import { AlocacaoDTO } from '../../models/dto/alocacao.dto';
import { RendaMensalDTO } from '../../models/dto/renda-mensal.dto';
import { RendaPorFundoDTO } from '../../models/dto/renda-por-fundo.dto';
import { ResumoCarteiraDTO } from '../../models/dto/resumo-carteira.dto';
import { RelatorioService } from '../../services/relatorio.service';

const PALETA = [
  '#3b82f6',
  '#10b981',
  '#f59e0b',
  '#ef4444',
  '#8b5cf6',
  '#ec4899',
  '#06b6d4',
  '#f97316',
  '#84cc16',
  '#a855f7',
  '#14b8a6'
];

@Component({
  selector: 'app-relatorios',
  imports: [CurrencyPipe, DecimalPipe, CardModule, ChartModule, SkeletonModule, TableModule, TagModule],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.scss'
})
export class RelatoriosComponent implements OnInit {
  private readonly relatorioService = inject(RelatorioService);
  private readonly messageService = inject(MessageService);
  private readonly errorService = inject(ErrorService);

  protected readonly loading = signal(true);
  protected readonly resumo = signal<ResumoCarteiraDTO | null>(null);
  protected readonly rendaPorFundo = signal<RendaPorFundoDTO[]>([]);

  protected readonly chartTipo = signal<unknown>(null);
  protected readonly chartSegmento = signal<unknown>(null);
  protected readonly chartRendaMensal = signal<unknown>(null);

  protected readonly pieOptions = this.buildPieOptions();
  protected readonly barOptions = this.buildBarOptions();

  ngOnInit(): void {
    forkJoin({
      resumo: this.relatorioService.resumoCarteira(),
      porTipo: this.relatorioService.alocacaoPorTipo(),
      porSegmento: this.relatorioService.alocacaoPorSegmento(),
      rendaMensal: this.relatorioService.rendaMensal(),
      rendaPorFundo: this.relatorioService.rendaPorFundo()
    }).subscribe({
      next: (d) => {
        this.resumo.set(d.resumo);
        this.chartTipo.set(this.buildPizzaData(d.porTipo));
        this.chartSegmento.set(this.buildPizzaData(d.porSegmento));
        this.chartRendaMensal.set(this.buildBarrasData(d.rendaMensal));
        this.rendaPorFundo.set(d.rendaPorFundo);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar relatórios');
      }
    });
  }

  private buildPizzaData(items: AlocacaoDTO[]): unknown {
    return {
      labels: items.map((i) => i.categoriaDescricao),
      datasets: [
        {
          data: items.map((i) => i.custoAtual),
          backgroundColor: items.map((_, idx) => PALETA[idx % PALETA.length]),
          borderWidth: 0
        }
      ]
    };
  }

  private buildBarrasData(items: RendaMensalDTO[]): unknown {
    const ordenado = [...items].reverse();
    return {
      labels: ordenado.map((i) => `${i.nomeMes.substring(0, 3)}/${String(i.ano).slice(-2)}`),
      datasets: [
        {
          label: 'Renda recebida (R$)',
          data: ordenado.map((i) => i.totalRecebido),
          backgroundColor: '#10b981',
          borderRadius: 4
        }
      ]
    };
  }

  private buildPieOptions(): unknown {
    const textColor = this.cssVar('--p-text-color', '#94a3b8');
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: textColor, font: { size: 12 } }
        },
        tooltip: {
          callbacks: {
            label: (ctx: { label: string; parsed: number }) => {
              const value = ctx.parsed.toLocaleString('pt-BR', {
                style: 'currency',
                currency: 'BRL'
              });
              return `${ctx.label}: ${value}`;
            }
          }
        }
      }
    };
  }

  private buildBarOptions(): unknown {
    const textColor = this.cssVar('--p-text-color', '#94a3b8');
    const mutedColor = this.cssVar('--p-text-muted-color', '#64748b');
    const gridColor = this.cssVar('--p-content-border-color', '#334155');
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx: { parsed: { y: number } }) =>
              ' ' +
              ctx.parsed.y.toLocaleString('pt-BR', {
                style: 'currency',
                currency: 'BRL'
              })
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            color: mutedColor,
            callback: (value: number) =>
              value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
          },
          grid: { color: gridColor }
        },
        x: {
          ticks: { color: textColor },
          grid: { display: false }
        }
      }
    };
  }

  private cssVar(name: string, fallback: string): string {
    if (typeof window === 'undefined') return fallback;
    const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    return value || fallback;
  }
}

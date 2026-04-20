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
import { FundoResponseDTO } from '../../models/dto/fundo-response.dto';
import { FundoService } from '../../services/fundo.service';

@Component({
  selector: 'app-fundo-list',
  imports: [
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
  templateUrl: './fundo-list.component.html',
  styleUrl: './fundo-list.component.scss'
})
export class FundoListComponent implements OnInit {
  private readonly fundoService = inject(FundoService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly errorService = inject(ErrorService);

  protected readonly fundos = signal<FundoResponseDTO[]>([]);
  protected readonly loading = signal(false);
  protected readonly globalFilter = signal('');

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.fundoService.listar(false).subscribe({
      next: (lista) => {
        this.fundos.set(lista);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.showToast(err, 'Erro ao carregar fundos');
      }
    });
  }

  editar(fundo: FundoResponseDTO): void {
    this.router.navigate(['/fundos', fundo.id, 'editar']);
  }

  confirmarDesativacao(fundo: FundoResponseDTO): void {
    this.confirmationService.confirm({
      header: 'Desativar fundo',
      message: `Tem certeza que deseja desativar o fundo ${fundo.ticker}? O registro sera mantido mas ficara inativo.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Desativar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.desativar(fundo)
    });
  }

  private desativar(fundo: FundoResponseDTO): void {
    this.fundoService.desativar(fundo.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Fundo desativado',
          detail: `${fundo.ticker} foi desativado com sucesso.`
        });
        this.carregar();
      },
      error: (err) => {
        this.errorService.showToast(err, 'Erro ao desativar');
      }
    });
  }
}

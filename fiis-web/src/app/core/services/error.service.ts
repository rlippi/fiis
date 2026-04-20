import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { MessageService } from 'primeng/api';

/**
 * Centraliza a tradução de HttpErrorResponse em mensagens amigáveis em PT-BR
 * e expõe um atalho para exibir toasts de erro padronizados.
 *
 * Todos os componentes que precisam mostrar erros de chamadas HTTP devem usar
 * esse serviço em vez de implementar seu próprio mapError, garantindo que o
 * tratamento fique consistente em toda a aplicação.
 */
@Injectable({ providedIn: 'root' })
export class ErrorService {
  private readonly messageService = inject(MessageService);

  mapMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Sem conexão com o servidor. Verifique sua internet ou tente novamente em alguns segundos.';
    }

    if (err.status === 400 || err.status === 409) {
      return err.error?.mensagem ?? 'Dados inválidos.';
    }

    if (err.status === 401) {
      return 'Sessão expirada. Faça login novamente.';
    }

    if (err.status === 403) {
      return 'Você não tem permissão para esta ação.';
    }

    if (err.status === 404) {
      return err.error?.mensagem ?? 'Recurso não encontrado.';
    }

    if (err.status === 502 || err.status === 503 || err.status === 504) {
      return 'Servidor reiniciando (cold start). Tente novamente em alguns segundos.';
    }

    if (err.status >= 500) {
      return 'Erro no servidor. Tente novamente ou contate o suporte.';
    }

    return err.error?.mensagem ?? 'Erro inesperado.';
  }

  showToast(err: HttpErrorResponse, summary: string = 'Erro'): void {
    this.messageService.add({
      severity: 'error',
      summary,
      detail: this.mapMessage(err)
    });
  }
}

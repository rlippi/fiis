import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Mock, vi } from 'vitest';
import { MessageService } from 'primeng/api';

import { ErrorService } from './error.service';

describe('ErrorService', () => {
  let service: ErrorService;
  let addMock: Mock;

  beforeEach(() => {
    addMock = vi.fn();
    TestBed.configureTestingModule({
      providers: [{ provide: MessageService, useValue: { add: addMock } }]
    });
    service = TestBed.inject(ErrorService);
  });

  function erroComStatus(status: number, body?: unknown): HttpErrorResponse {
    return new HttpErrorResponse({ status, error: body, statusText: 'mock' });
  }

  describe('mapMessage', () => {
    it('status 0 retorna mensagem de sem conexão', () => {
      expect(service.mapMessage(erroComStatus(0))).toContain('Sem conexão');
    });

    it('status 401 retorna sessão expirada', () => {
      expect(service.mapMessage(erroComStatus(401))).toContain('Sessão expirada');
    });

    it('status 403 retorna sem permissão', () => {
      expect(service.mapMessage(erroComStatus(403))).toContain('permissão');
    });

    it('status 400 prioriza err.error.mensagem do backend', () => {
      const msg = 'Email já cadastrado';
      expect(service.mapMessage(erroComStatus(400, { mensagem: msg }))).toBe(msg);
    });

    it('status 429 retorna mensagem do backend ou fallback amigável', () => {
      expect(service.mapMessage(erroComStatus(429))).toContain('tentativas');
    });

    it('status 502/503/504 retorna mensagem de cold start', () => {
      expect(service.mapMessage(erroComStatus(503))).toContain('cold start');
    });

    it('status 500 sem código tratado retorna mensagem genérica de servidor', () => {
      expect(service.mapMessage(erroComStatus(500))).toContain('servidor');
    });
  });

  describe('showToast', () => {
    it('dispara MessageService.add com severity=error e detail mapeado', () => {
      service.showToast(erroComStatus(401), 'Falha no login');

      expect(addMock).toHaveBeenCalledTimes(1);
      const args = addMock.mock.calls[0][0];
      expect(args.severity).toBe('error');
      expect(args.summary).toBe('Falha no login');
      expect(args.detail).toContain('Sessão expirada');
    });

    it('quando err.error é Blob com JSON, parseia e usa mensagem do backend', async () => {
      const blob = new Blob([JSON.stringify({ mensagem: 'Recurso bloqueado' })], {
        type: 'application/json'
      });
      const erro = new HttpErrorResponse({ status: 409, error: blob, statusText: 'conflict' });

      service.showToast(erro, 'Erro');

      // dispatch é assíncrono (lê o blob via promise) — esperar um tick
      await new Promise((resolve) => setTimeout(resolve, 10));

      expect(addMock).toHaveBeenCalledTimes(1);
      expect(addMock.mock.calls[0][0].detail).toBe('Recurso bloqueado');
    });
  });
});

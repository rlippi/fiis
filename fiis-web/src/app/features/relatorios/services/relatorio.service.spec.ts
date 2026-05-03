import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { environment } from '../../../../environments/environment';
import { RelatorioService } from './relatorio.service';

describe('RelatorioService', () => {
  let service: RelatorioService;
  let http: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/api/relatorios`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RelatorioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('exportarPosicaoPdf chama o endpoint .pdf com responseType blob e observe response', () => {
    service.exportarPosicaoPdf().subscribe();

    const req = http.expectOne(`${baseUrl}/posicao.pdf`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['fake-pdf'], { type: 'application/pdf' }));
  });

  it('exportarPosicaoXlsx chama o endpoint .xlsx com responseType blob', () => {
    service.exportarPosicaoXlsx().subscribe();

    const req = http.expectOne(`${baseUrl}/posicao.xlsx`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['fake-xlsx']));
  });

  it('resumoCarteira chama o endpoint /resumo-carteira', () => {
    service.resumoCarteira().subscribe();

    const req = http.expectOne(`${baseUrl}/resumo-carteira`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});

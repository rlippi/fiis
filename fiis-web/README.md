# fiis-web

Frontend do gerenciador de carteira de FIIs.

**Stack:** Angular 21 (standalone, signals, zoneless) · PrimeNG 21 (Aura) · Chart.js · SCSS · Vitest

> Para a visão geral do projeto e setup completo (backend + frontend + Docker), veja o [README na raiz](../README.md). Decisões de arquitetura interna estão em [`ARCHITECTURE.md`](./ARCHITECTURE.md).

## Como rodar

```bash
npm install     # primeira vez
npm start
```

Disponível em `http://localhost:4200`.

## Scripts

| Script | O que faz |
|---|---|
| `npm start` | Dev server com hot reload, aponta para a API local em `http://localhost:8081` |
| `npm run start:hml` | Dev server apontando para a API em HML (`https://fiis-api-hml.onrender.com`) |
| `npm run build` | Build de produção (env `production`) |
| `npm run build:hml` | Build apontando para a API HML — usado pelo Vercel |
| `npm test` | Roda os testes (Vitest) |

## Tema claro / escuro / sistema

3 modos persistidos em `localStorage` e gerenciados pelo `ThemeService` ([core/services/theme.service.ts](src/app/core/services/theme.service.ts)).

## Integração com o backend

| Ambiente | URL da API | Configurado em |
|---|---|---|
| Dev local | `http://localhost:8081` | `src/environments/environment.ts` |
| HML | `https://fiis-api-hml.onrender.com` | `src/environments/environment.hml.ts` |

## Estrutura de código

- `src/app/core` — services, guards, interceptors, models compartilhados
- `src/app/shared` — layout (sidebar, header) e componentes reutilizáveis
- `src/app/features/<dominio>` — domínios isolados (auth, fundos, operacoes, proventos, cotacoes, eventos, relatorios)

Regras de dependência detalhadas em [`ARCHITECTURE.md`](./ARCHITECTURE.md).

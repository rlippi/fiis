# fiis-web

Frontend do sistema de controle de investimentos em FIIs.

**Stack:** Angular 21 · PrimeNG 21 · Signals · SCSS

Arquitetura detalhada: [ARCHITECTURE.md](./ARCHITECTURE.md).

## Como rodar

```bash
npm install
npm start
```

Abre em `http://localhost:4200` (ou outra porta se a 4200 estiver ocupada).

## Scripts

| Script | O que faz |
|---|---|
| `npm start` | Dev server com hot reload (`ng serve`) |
| `npm run build` | Build de produção |
| `npm run watch` | Build dev em modo watch |
| `npm test` | Roda os testes (Vitest) |

## Tema claro/escuro/sistema

O app suporta 3 modos: **Sistema** (default — segue o OS), **Claro** e **Escuro**. A preferência é persistida em `localStorage` e gerenciada pelo `ThemeService` (ver `src/app/core/services/theme.service.ts`).

## Integração com o backend (fiis-api)

| Ambiente | URL |
|---|---|
| Dev local | `http://localhost:8081` |
| HML | `https://fiis-api-hml.onrender.com` |

As URLs são configuradas via `src/environments/` (a ser adicionado).

## Estrutura de código

Resumo: `src/app/core` (infra), `src/app/shared` (reutilizáveis), `src/app/features` (domínios). Detalhes e regras de dependência em [ARCHITECTURE.md](./ARCHITECTURE.md).

# fiis-web — Arquitetura

## Stack

- **Angular 21** (standalone components, zoneless, Signals)
- **PrimeNG 21** + `@primeuix/themes` (preset Aura) + primeicons
- **SCSS** para estilos
- **esbuild** (via `@angular/build`)
- **Vitest** para testes
- **HttpClient** do Angular + Interceptor JWT (a adicionar na FASE 4)

## Estrutura de pastas

```
src/app/
├── core/          Infra transversal (uma instância só no app)
│   ├── services/      Ex: ThemeService, AuthService
│   ├── guards/        Ex: AuthGuard, GuestGuard
│   ├── interceptors/  Ex: JwtInterceptor
│   └── models/        Tipos/interfaces compartilhadas
│
├── shared/        Componentes/pipes/diretivas reutilizáveis (sem lógica de domínio)
│   ├── components/    Ex: <app-money>, <app-empty-state>
│   ├── directives/    Ex: appAutofocus
│   └── pipes/         Ex: timeAgoPipe, currencyBrlPipe
│
└── features/      Funcionalidades do domínio (rotas lazy-loaded)
    ├── auth/          login, logout, refresh token
    ├── fundos/        CRUD de fundos imobiliários
    ├── operacoes/     compras e vendas
    ├── proventos/     dividendos recebidos
    ├── cotacoes/      histórico de cotações
    ├── eventos/       eventos corporativos
    └── relatorios/    dashboards e exportações
```

## Regras de dependência

1. **`core/` é singleton.** Tudo lá é `providedIn: 'root'` e importado uma única vez no bootstrap.
2. **`shared/` não importa de `core/` nem de `features/`.** Só depende do Angular/PrimeNG.
3. **`features/` pode importar de `core/` e `shared/`,** mas nunca de outra feature. Se duas features precisarem do mesmo, o compartilhado sobe para `shared/` ou `core/`.
4. **Cada feature é lazy-loaded** via `loadChildren` em `app.routes.ts`.
5. **Signals por padrão.** RxJS só quando houver streams reais (HTTP, eventos, debouncing).

## Tema

Três modos suportados: `system`, `light`, `dark`. Gerenciados pelo `ThemeService` em `core/services/theme.service.ts`:

- Preferência salva em `localStorage` (chave `fiis-theme`)
- Quando em `system`, escuta `prefers-color-scheme` e reage em runtime
- Aplica/remove a classe `.app-dark` no `<html>`; o PrimeNG Aura usa essa classe para trocar o preset

## Integração com a API (fiis-api)

| Ambiente | URL |
|---|---|
| Dev local | `http://localhost:8081` |
| HML | `https://fiis-api-hml.onrender.com` |

- Autenticação: `POST /auth/login` retorna JWT (HS256, TTL 24h)
- Interceptor JWT adiciona `Authorization: Bearer <token>` automaticamente
- URLs configuradas em `src/environments/` (a ser criado no Passo 6 da FASE 4)

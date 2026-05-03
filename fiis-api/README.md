# fiis-api

Backend do gerenciador de carteira de FIIs.

**Stack:** Java 21 · Spring Boot 3.2 · Spring Security · JWT · JPA / Hibernate · Flyway · PostgreSQL · MapStruct · Lombok

> Para a visão geral do projeto e setup completo (backend + frontend + Docker), veja o [README na raiz](../README.md).

## Como rodar

```bash
mvn spring-boot:run
```

Disponível em `http://localhost:8081`. Swagger em [`/swagger-ui/index.html`](http://localhost:8081/swagger-ui/index.html).

## Variáveis de ambiente úteis

| Variável | Default | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil ativo (`dev`, `hml`, `prod`) |
| `FIIS_ADMIN_EMAIL` | — | E-mail do administrador inicial. Sem isso, o seed do admin é ignorado |
| `FIIS_ADMIN_PASSWORD` | — | Senha do administrador inicial |
| `FIIS_SEED_DEMO_ENABLED` | `false` | Liga o seed do usuário demo (`demo@fiis.com`) com carteira de exemplo |
| `FIIS_JWT_SECRET` | (dev) | Segredo HMAC-SHA256 para JWT — obrigatório em prod (≥ 32 bytes) |
| `BRAPI_TOKEN` | — | Token da [BRAPI](https://brapi.dev) (free tier funciona sem) |
| `SPRING_MAIL_USERNAME` / `..._PASSWORD` | — | SMTP para emails transacionais (esqueci senha). Em dev, deixe vazio + `FIIS_MAIL_ENABLED=false` |
| `FIIS_JOB_ATUALIZAR_COTACOES_ENABLED` | `true` | Liga/desliga o job agendado diário sem redeploy |

## Testes

```bash
mvn test
```

Os testes de integração usam `application-test.properties`, que aponta para o banco `fiis_test` no PostgreSQL local — mantenha o container Docker rodando.

## Migrations (Flyway)

Migrations em `src/main/resources/db/migration/V*__*.sql`. São aplicadas automaticamente no startup. Em ambientes com schema legado, `baseline-on-migrate=true` permite adoção sem reset.

## Convenções

- **Pacotes**: `domain/dto` (saídas), `domain/vo` (entradas), `domain/entity`, `domain/event`, `domain/mapper` (MapStruct), `domain/enumeration`. Cross-cutting concerns em `support/`, `audit/`, `metrics/`, `export/`.
- **`package-info.java`** em cada pacote, descrevendo a responsabilidade.
- **Mensagens de erro** centralizadas em `messages_pt_BR.properties` com códigos `FIIxxxx`.

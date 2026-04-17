# FIIs — Gerenciador de Carteira de Fundos Imobiliários

Aplicação para gestão de carteira de **FIIs (Fundos de Investimento Imobiliário)** brasileiros, com controle de operações, proventos, indicadores e análise de alocação.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3 + Maven |
| Frontend | Angular 17+ (em breve) |
| Banco de dados | PostgreSQL 16 |
| Infra local | Docker (PostgreSQL) |
| Deploy | Render (backend) + Vercel (frontend) + Supabase (banco) |

## Estrutura do projeto (monorepo)

```
fiis/
├── fiis-api/       # Backend Java + Spring Boot
└── fiis-web/       # Frontend Angular
```

## Ambientes

O projeto usa **Spring Profiles** para três ambientes:

- `dev` — desenvolvimento local (PostgreSQL via Docker)
- `hml` — homologação na nuvem (PostgreSQL no Supabase)
- `prod` — produção na nuvem (PostgreSQL no Supabase)

## Como rodar localmente

### Pré-requisitos
- Java 21
- Maven 3.9+
- Docker Desktop

### 1. Subir o PostgreSQL (Docker)

```bash
docker start fiis-postgres
```

Primeira vez:
```bash
docker run -d --name fiis-postgres \
  -e POSTGRES_DB=fiis \
  -e POSTGRES_USER=renlip \
  -e POSTGRES_PASSWORD=fiis123 \
  -p 5432:5432 \
  -v fiis-postgres-data:/var/lib/postgresql/data \
  postgres:16-alpine
```

### 2. Rodar o Backend

```bash
cd fiis-api
mvn spring-boot:run
```

API disponível em: **http://localhost:8081**

Endpoint de teste: **http://localhost:8081/api/health**

## Convenção de commits

Este projeto segue [Conventional Commits](https://www.conventionalcommits.org/pt-br/) em português:

- `feat(api):` nova funcionalidade no backend
- `feat(web):` nova funcionalidade no frontend
- `fix:` correção de bug
- `docs:` documentação
- `chore:` configs, build, dependências
- `refactor:` refatoração
- `test:` testes

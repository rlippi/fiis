-- ============================================================================
-- V1__baseline.sql
-- ----------------------------------------------------------------------------
-- Baseline inicial do schema do fiis-api. Reflete o estado gerado pelo
-- Hibernate a partir das entidades atuais (FASES 1 a 5 já aplicadas) e
-- passa a ser a primeira migration gerenciada pelo Flyway.
--
-- Tabelas:
--   - usuario               (FASE 2 - Spring Security + JWT)
--   - fundo                 (cadastro dos FIIs)
--   - operacao              (compras e vendas)
--   - provento              (rendimentos e amortizações)
--   - cotacao               (histórico de preços)
--   - evento_corporativo    (desdobramentos, grupamentos, bonificações)
--
-- Integridade:
--   - FK cotacao.fundo_id            -> fundo(id)
--   - FK evento_corporativo.fundo_id -> fundo(id)
--   - FK operacao.fundo_id           -> fundo(id)
--   - FK provento.fundo_id           -> fundo(id)
--
-- Notas:
--   - Em bancos que já possuíam este schema (HML no Supabase), o Flyway é
--     configurado com baseline-on-migrate=true. Nesse caso, essa migration
--     é apenas registrada no flyway_schema_history, sem re-executar os
--     CREATE TABLE (já existentes).
--   - A partir desta V1, toda mudança de schema deve vir como uma nova
--     migration versionada (V2__..., V3__..., etc).
-- ============================================================================

create table usuario (
    id               bigserial     not null,
    nome             varchar(100)  not null,
    email            varchar(150)  not null unique,
    senha            varchar(100)  not null,
    perfil           varchar(20)   not null check (perfil in ('USER', 'ADMIN')),
    ativo            boolean       not null,
    data_criacao     timestamp(6)  not null,
    data_atualizacao timestamp(6)  not null,
    primary key (id)
);

create table fundo (
    id               bigserial     not null,
    ticker           varchar(10)   not null unique,
    nome             varchar(150)  not null,
    cnpj             varchar(14),
    tipo             varchar(30)   not null check (tipo in ('TIJOLO', 'PAPEL', 'HIBRIDO', 'FUNDO_DE_FUNDOS')),
    segmento         varchar(30)   not null check (segmento in ('LOGISTICA', 'SHOPPING', 'LAJES_CORPORATIVAS', 'RESIDENCIAL', 'HOSPITALAR', 'EDUCACIONAL', 'AGENCIAS_BANCARIAS', 'HOTEIS', 'RECEBIVEIS', 'MULTISEGMENTO', 'FUNDO_DE_FUNDOS')),
    ativo            boolean       not null,
    data_criacao     timestamp(6)  not null,
    data_atualizacao timestamp(6)  not null,
    primary key (id)
);

create table operacao (
    id               bigserial     not null,
    fundo_id         bigint        not null,
    tipo             varchar(10)   not null check (tipo in ('COMPRA', 'VENDA')),
    data_operacao    date          not null,
    quantidade       integer       not null,
    preco_unitario   numeric(15,4) not null,
    taxas            numeric(10,2),
    observacao       varchar(255),
    data_criacao     timestamp(6)  not null,
    data_atualizacao timestamp(6)  not null,
    primary key (id)
);

create table provento (
    id                bigserial     not null,
    fundo_id          bigint        not null,
    tipo_provento     varchar(20)   not null check (tipo_provento in ('RENDIMENTO', 'AMORTIZACAO')),
    data_referencia   date          not null,
    data_pagamento    date          not null,
    quantidade_cotas  integer       not null,
    valor_por_cota    numeric(15,6) not null,
    observacao        varchar(255),
    data_criacao      timestamp(6)  not null,
    data_atualizacao  timestamp(6)  not null,
    primary key (id)
);

create table cotacao (
    id                bigserial     not null,
    fundo_id          bigint        not null,
    data              date          not null,
    preco_fechamento  numeric(15,4) not null,
    preco_abertura    numeric(15,4),
    preco_minimo      numeric(15,4),
    preco_maximo      numeric(15,4),
    volume            numeric(18,2),
    data_criacao      timestamp(6)  not null,
    data_atualizacao  timestamp(6)  not null,
    primary key (id),
    constraint uk_cotacao_fundo_data unique (fundo_id, data)
);

create table evento_corporativo (
    id               bigserial     not null,
    fundo_id         bigint        not null,
    tipo             varchar(20)   not null check (tipo in ('BONIFICACAO', 'DESDOBRAMENTO', 'GRUPAMENTO')),
    data             date          not null,
    fator            numeric(10,6) not null,
    descricao        varchar(255),
    data_criacao     timestamp(6)  not null,
    data_atualizacao timestamp(6)  not null,
    primary key (id)
);

-- Foreign keys
alter table cotacao
    add constraint fk_cotacao_fundo
    foreign key (fundo_id) references fundo (id);

alter table evento_corporativo
    add constraint fk_evento_corporativo_fundo
    foreign key (fundo_id) references fundo (id);

alter table operacao
    add constraint fk_operacao_fundo
    foreign key (fundo_id) references fundo (id);

alter table provento
    add constraint fk_provento_fundo
    foreign key (fundo_id) references fundo (id);

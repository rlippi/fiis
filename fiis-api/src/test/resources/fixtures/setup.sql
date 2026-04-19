-- ============================================
-- SETUP — Preparação do banco de testes
-- ============================================
-- Executado ANTES de cada método de teste (@Sql com BEFORE_TEST_METHOD),
-- garantindo que o banco esteja em estado conhecido:
--
--   1) DDL  — cria as tabelas do domínio se ainda não existirem.
--             (Normalmente o Hibernate já criou via ddl-auto=create-drop,
--             então os CREATE TABLE IF NOT EXISTS funcionam como rede
--             de segurança e documentação do esquema.)
--
--   2) RESET — trunca todas as tabelas e reinicia as sequências,
--             garantindo IDs previsíveis para comparação com os
--             arquivos expected.json.
-- ============================================


-- ============================================
-- DDL — Criação das tabelas e constraints necessárias para os testes
-- ============================================

CREATE TABLE IF NOT EXISTS fundo (
    id                  BIGSERIAL PRIMARY KEY,
    ticker              VARCHAR(10)  NOT NULL UNIQUE,
    nome                VARCHAR(150) NOT NULL,
    cnpj                VARCHAR(14),
    tipo                VARCHAR(30)  NOT NULL,
    segmento            VARCHAR(30)  NOT NULL,
    ativo               BOOLEAN      NOT NULL,
    data_criacao        TIMESTAMP    NOT NULL,
    data_atualizacao    TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS operacao (
    id                  BIGSERIAL PRIMARY KEY,
    fundo_id            BIGINT        NOT NULL REFERENCES fundo (id),
    tipo                VARCHAR(10)   NOT NULL,
    data_operacao       DATE          NOT NULL,
    quantidade          INTEGER       NOT NULL,
    preco_unitario      NUMERIC(15,4) NOT NULL,
    taxas               NUMERIC(10,2),
    observacao          VARCHAR(255),
    data_criacao        TIMESTAMP     NOT NULL,
    data_atualizacao    TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS provento (
    id                  BIGSERIAL PRIMARY KEY,
    fundo_id            BIGINT        NOT NULL REFERENCES fundo (id),
    tipo_provento       VARCHAR(20)   NOT NULL,
    data_referencia     DATE          NOT NULL,
    data_pagamento      DATE          NOT NULL,
    valor_por_cota      NUMERIC(15,6) NOT NULL,
    quantidade_cotas    INTEGER       NOT NULL,
    observacao          VARCHAR(255),
    data_criacao        TIMESTAMP     NOT NULL,
    data_atualizacao    TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS cotacao (
    id                  BIGSERIAL PRIMARY KEY,
    fundo_id            BIGINT        NOT NULL REFERENCES fundo (id),
    data                DATE          NOT NULL,
    preco_fechamento    NUMERIC(15,4) NOT NULL,
    preco_abertura      NUMERIC(15,4),
    preco_minimo        NUMERIC(15,4),
    preco_maximo        NUMERIC(15,4),
    volume              NUMERIC(18,2),
    data_criacao        TIMESTAMP     NOT NULL,
    data_atualizacao    TIMESTAMP     NOT NULL,
    CONSTRAINT uk_cotacao_fundo_data UNIQUE (fundo_id, data)
);

CREATE TABLE IF NOT EXISTS evento_corporativo (
    id                  BIGSERIAL PRIMARY KEY,
    fundo_id            BIGINT        NOT NULL REFERENCES fundo (id),
    tipo                VARCHAR(20)   NOT NULL,
    data                DATE          NOT NULL,
    fator               NUMERIC(10,6) NOT NULL,
    descricao           VARCHAR(255),
    data_criacao        TIMESTAMP     NOT NULL,
    data_atualizacao    TIMESTAMP     NOT NULL
);


-- ============================================
-- RESET — Limpa todas as tabelas e reinicia as sequências de IDs
-- ============================================
-- CASCADE remove também os registros filhos das tabelas relacionadas.
-- RESTART IDENTITY reinicia os BIGSERIAL, garantindo que os IDs sejam
-- previsíveis (1, 2, 3, ...) a cada execução de teste.

TRUNCATE TABLE
    operacao,
    provento,
    cotacao,
    evento_corporativo,
    fundo
RESTART IDENTITY CASCADE;

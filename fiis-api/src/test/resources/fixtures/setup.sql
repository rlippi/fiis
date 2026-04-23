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
--
--   3) USUÁRIO DE TESTE — insere o usuário `test@fiis.com` (ID = 1),
--             referenciado por `@WithUserDetails("test@fiis.com")` no
--             AbstractControllerTests. Todos os INSERTs dos fii-script.sql
--             associam os dados a esse usuário via `usuario_id = 1`.
-- ============================================


-- ============================================
-- DDL — Criação das tabelas e constraints necessárias para os testes
-- ============================================

CREATE TABLE IF NOT EXISTS usuario (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    senha               VARCHAR(100) NOT NULL,
    perfil              VARCHAR(20)  NOT NULL,
    ativo               BOOLEAN      NOT NULL,
    data_criacao        TIMESTAMP    NOT NULL,
    data_atualizacao    TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS fundo (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT       NOT NULL REFERENCES usuario (id),
    ticker              VARCHAR(10)  NOT NULL,
    nome                VARCHAR(150) NOT NULL,
    cnpj                VARCHAR(14),
    tipo                VARCHAR(30)  NOT NULL,
    segmento            VARCHAR(30)  NOT NULL,
    ativo               BOOLEAN      NOT NULL,
    data_criacao        TIMESTAMP    NOT NULL,
    data_atualizacao    TIMESTAMP    NOT NULL,
    CONSTRAINT uk_fundo_usuario_ticker UNIQUE (usuario_id, ticker)
);

CREATE TABLE IF NOT EXISTS operacao (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT        NOT NULL REFERENCES usuario (id),
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
    usuario_id          BIGINT        NOT NULL REFERENCES usuario (id),
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
    usuario_id          BIGINT        NOT NULL REFERENCES usuario (id),
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
    usuario_id          BIGINT        NOT NULL REFERENCES usuario (id),
    fundo_id            BIGINT        NOT NULL REFERENCES fundo (id),
    tipo                VARCHAR(20)   NOT NULL,
    data                DATE          NOT NULL,
    fator               NUMERIC(10,6) NOT NULL,
    descricao           VARCHAR(255),
    data_criacao        TIMESTAMP     NOT NULL,
    data_atualizacao    TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS reset_token (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT        NOT NULL REFERENCES usuario (id),
    token               VARCHAR(64)   NOT NULL UNIQUE,
    expires_at          TIMESTAMP     NOT NULL,
    used_at             TIMESTAMP,
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
    fundo,
    reset_token,
    usuario
RESTART IDENTITY CASCADE;


-- ============================================
-- USUÁRIO DE TESTE — ID 1, referenciado por @WithUserDetails nos controllers
-- ============================================
-- A senha abaixo é um placeholder (hash BCrypt inválido). Os testes que
-- precisam validar login real sobrescrevem esse usuário no @BeforeEach
-- (ver AutenticacaoControllerTests).

INSERT INTO usuario (
    nome, email, senha, perfil, ativo, data_criacao, data_atualizacao
) VALUES
    ('Usuário de Teste', 'test@fiis.com', 'placeholder-nao-usado', 'USER', TRUE,
     '2026-01-01 10:00:00', '2026-01-01 10:00:00');

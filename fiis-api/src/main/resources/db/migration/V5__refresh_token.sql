-- ============================================================================
-- V5__refresh_token.sql
-- Cria a tabela refresh_token usada pelo fluxo de Refresh Token JWT (FASE 6.8).
--
-- Cada registro representa um refresh token emitido para um usuário:
--   * token_hash      — SHA-256 do token raw enviado ao cliente. O raw nunca
--                       é persistido — comprometimento do banco não vira
--                       comprometimento de tokens ativos. Hash hex = 64 chars.
--   * expires_at      — momento a partir do qual o token não vale (TTL longo,
--                       7 dias por default vs 15min do access).
--   * used_at         — preenchido quando o token é rotacionado em /refresh.
--                       Tentar usar de novo dispara reuse detection (sinal de
--                       roubo) e invalida TODOS os refresh do usuário.
--   * revoked_at      — preenchido em logout explícito. Distinto de used_at
--                       PROPOSITALMENTE: tentar usar um token revogado por
--                       logout falha sem disparar reuse detection (não invalida
--                       outros dispositivos do mesmo usuário).
--   * replaced_by_id  — auto-referência que rastreia a chain de rotação;
--                       permite auditoria de "quem veio depois de quem".
--
-- Estados possíveis (ordenados por precedência na validação):
--   1. Inválido por revogação:  revoked_at IS NOT NULL
--   2. Inválido por uso prévio: used_at IS NOT NULL  (gatilho de reuse detection)
--   3. Inválido por expiração:  expires_at < now()
--   4. Válido para rotação:     used_at IS NULL AND revoked_at IS NULL AND expires_at > now()
--
-- Regras cobertas pelo service (não pelo schema):
--   * Geração: SecureRandom 32 bytes, base64-url-safe, hash SHA-256 persistido.
--   * Rotação: ao consumir token válido, marca used_at, cria novo token e
--              preenche replaced_by_id apontando para o novo.
--   * Reuse detection: encontrar um token com used_at preenchido em /refresh
--              invalida (revoked_at = now) todos os tokens não-revogados do mesmo usuário.
-- ============================================================================

create table refresh_token (
    id               bigserial     not null,
    usuario_id       bigint        not null,
    token_hash       varchar(64)   not null unique,
    expires_at       timestamp(6)  not null,
    used_at          timestamp(6),
    revoked_at       timestamp(6),
    replaced_by_id   bigint,
    data_criacao     timestamp(6)  not null,
    data_atualizacao timestamp(6)  not null,
    primary key (id)
);

alter table refresh_token
    add constraint fk_refresh_token_usuario
    foreign key (usuario_id) references usuario (id);

alter table refresh_token
    add constraint fk_refresh_token_replaced_by
    foreign key (replaced_by_id) references refresh_token (id);

-- Caminho mais comum: validar um token recebido (busca por hash). Já é único,
-- então o índice gerado pelo UNIQUE basta — não criamos extra.
-- Reuse detection e logout-all consultam por usuario_id; índice ajuda.
create index idx_refresh_token_usuario_id on refresh_token (usuario_id);

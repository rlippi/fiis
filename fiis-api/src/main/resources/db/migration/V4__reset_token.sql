-- ============================================================================
-- V4__reset_token.sql
-- Cria a tabela reset_token usada pelo fluxo de "Esqueci minha senha".
--
-- Cada registro representa uma solicitação de reset:
--   * token      — UUID v4 gerado na criação, enviado ao usuário por email
--   * expires_at — momento a partir do qual o token não é mais válido (TTL)
--   * used_at    — preenchido quando o token é consumido (single-use)
--
-- Regras de negócio cobertas pelo service (não pelo schema):
--   * Ao criar um novo token para o mesmo usuário, os anteriores são
--     invalidados (used_at = now()) para evitar paralelismo.
--   * Um token é válido apenas se: existe, used_at IS NULL, expires_at > now().
-- ============================================================================

create table reset_token (
    id               bigserial     not null,
    usuario_id       bigint        not null,
    token            varchar(64)   not null unique,
    expires_at       timestamp(6)  not null,
    used_at          timestamp(6),
    data_criacao     timestamp(6)  not null,
    data_atualizacao timestamp(6)  not null,
    primary key (id)
);

alter table reset_token
    add constraint fk_reset_token_usuario
    foreign key (usuario_id) references usuario (id);

-- Índice para o caminho mais comum: buscar por usuario_id + not used + not expired.
-- Como praticamente toda consulta começa pelo usuário, o índice acelera esses filtros.
create index idx_reset_token_usuario_id on reset_token (usuario_id);

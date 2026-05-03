-- ============================================================================
-- V2__multi_usuario.sql
-- Introduz suporte a multi-usuário: cada Fundo, Operação, Provento, Cotação
-- e Evento Corporativo passa a pertencer a um usuário específico (usuario_id).
-- Dados pré-existentes são atribuídos ao primeiro usuário ADMIN encontrado.
-- Também substitui a unicidade global de fundo.ticker por (usuario_id, ticker),
-- permitindo que cada usuário tenha seu próprio HGLG11 na carteira dele.
-- ============================================================================

-- 1) Adiciona coluna usuario_id nullable em todas as tabelas de domínio.
alter table fundo              add column usuario_id bigint;
alter table operacao           add column usuario_id bigint;
alter table provento           add column usuario_id bigint;
alter table cotacao            add column usuario_id bigint;
alter table evento_corporativo add column usuario_id bigint;

-- 2) Atribui todos os dados existentes ao primeiro usuário ADMIN.
--    Se não houver admin e já existirem dados, aborta com erro claro para
--    que o operador crie um admin antes de migrar.
do $$
declare
    admin_id bigint;
begin
    select id into admin_id
      from usuario
     where perfil = 'ADMIN'
     order by id
     limit 1;

    if admin_id is null then
        if exists (select 1 from fundo)
           or exists (select 1 from operacao)
           or exists (select 1 from provento)
           or exists (select 1 from cotacao)
           or exists (select 1 from evento_corporativo) then
            raise exception 'Nao ha usuario ADMIN para atribuir os dados existentes. '
                            'Crie um admin (via FIIS_ADMIN_EMAIL/FIIS_ADMIN_PASSWORD) antes de rodar a V2.';
        end if;
    else
        update fundo              set usuario_id = admin_id where usuario_id is null;
        update operacao           set usuario_id = admin_id where usuario_id is null;
        update provento           set usuario_id = admin_id where usuario_id is null;
        update cotacao            set usuario_id = admin_id where usuario_id is null;
        update evento_corporativo set usuario_id = admin_id where usuario_id is null;
    end if;
end $$;

-- 3) Torna usuario_id obrigatório (NOT NULL).
alter table fundo              alter column usuario_id set not null;
alter table operacao           alter column usuario_id set not null;
alter table provento           alter column usuario_id set not null;
alter table cotacao            alter column usuario_id set not null;
alter table evento_corporativo alter column usuario_id set not null;

-- 4) Foreign keys para a tabela usuario.
alter table fundo
    add constraint fk_fundo_usuario
    foreign key (usuario_id) references usuario (id);

alter table operacao
    add constraint fk_operacao_usuario
    foreign key (usuario_id) references usuario (id);

alter table provento
    add constraint fk_provento_usuario
    foreign key (usuario_id) references usuario (id);

alter table cotacao
    add constraint fk_cotacao_usuario
    foreign key (usuario_id) references usuario (id);

alter table evento_corporativo
    add constraint fk_evento_corporativo_usuario
    foreign key (usuario_id) references usuario (id);

-- 5) Unicidade de fundo.ticker passa a ser por usuário.
--    A constraint antiga foi criada implicitamente pelo PostgreSQL como
--    "fundo_ticker_key" (padrão <tabela>_<coluna>_key). Usamos IF EXISTS
--    para tolerar ambientes onde tenha outro nome.
alter table fundo drop constraint if exists fundo_ticker_key;

alter table fundo
    add constraint uk_fundo_usuario_ticker
    unique (usuario_id, ticker);

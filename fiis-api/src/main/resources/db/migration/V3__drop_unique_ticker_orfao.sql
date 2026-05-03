-- ============================================================================
-- V3__drop_unique_ticker_orfao.sql
-- Remove qualquer UNIQUE constraint remanescente em fundo(ticker) que não
-- seja a nova uk_fundo_usuario_ticker. Necessário porque a V2 tentou dropar
-- a constraint com nome fixo "fundo_ticker_key" (padrão PostgreSQL para
-- UNIQUE inline), mas em ambientes cujo schema foi criado pelo Hibernate a
-- constraint pode ter recebido outro nome (ex: "ukXXXXX"). Nesses casos o
-- DROP IF EXISTS da V2 não encontrou nada e a unicidade antiga (só ticker)
-- continuou ativa, bloqueando usuários distintos de cadastrar o mesmo FII.
-- ============================================================================

do $$
declare
    nome_constraint text;
begin
    for nome_constraint in
        select c.conname
          from pg_constraint c
          join pg_attribute a on a.attrelid = c.conrelid and a.attnum = any(c.conkey)
         where c.conrelid = 'fundo'::regclass
           and c.contype = 'u'
           and array_length(c.conkey, 1) = 1
           and a.attname = 'ticker'
           and c.conname <> 'uk_fundo_usuario_ticker'
    loop
        execute 'alter table fundo drop constraint ' || quote_ident(nome_constraint);
    end loop;
end $$;

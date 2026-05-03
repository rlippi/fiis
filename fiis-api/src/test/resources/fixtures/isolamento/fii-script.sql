-- ============================================
-- FII-SCRIPT — Massa para testes de isolamento multi-usuário
-- ============================================
-- Executado APÓS o setup.sql (TRUNCATE + RESTART IDENTITY + usuário de teste ID=1
-- 'test@fiis.com'). Aqui adicionamos um segundo usuário 'outro@fiis.com' (ID=2)
-- e populamos a carteira do primeiro usuário, para validar que o segundo não
-- consegue ver ou mexer nos dados do primeiro.
--
--   Usuários:
--     ID 1 - test@fiis.com   (USER, já criado no setup.sql)
--     ID 2 - outro@fiis.com  (USER, criado aqui)
--
--   Fundos do USUÁRIO 1 (test):
--     ID 1 - HGLG11 (ativo)
--     ID 2 - MXRF11 (ativo)
-- ============================================

INSERT INTO usuario (
    nome, email, senha, perfil, ativo, data_criacao, data_atualizacao
) VALUES
    ('Outro Usuário', 'outro@fiis.com', 'placeholder-nao-usado', 'USER', TRUE,
     '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO fundo (
    usuario_id, ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    (1, 'HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    (1, 'MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO operacao (
    usuario_id, fundo_id, tipo, data_operacao, quantidade, preco_unitario, taxas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 1, 'COMPRA', '2026-03-15', 10, 150.25, 0.50, NULL, '2026-03-15 10:00:00', '2026-03-15 10:00:00');

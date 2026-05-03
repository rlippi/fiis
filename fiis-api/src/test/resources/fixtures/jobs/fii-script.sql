-- ============================================
-- FII-SCRIPT — Massa para testes do JobController
-- ============================================
-- Executado APÓS o setup.sql (que cria test@fiis.com ID=1, USER, ativo).
--
-- Adiciona:
--   ID 2 - admin@fiis.com (ADMIN, ativo) — usado em @WithUserDetails nos testes de 200
--   ID 3 - outro@fiis.com (USER,  ativo)
--
-- Cada um dos 3 usuários recebe 1 fundo ativo com ticker distinto:
--   Fundo 1 (usuario 1 / test):  HGLG11
--   Fundo 2 (usuario 2 / admin): FAIL11   ← usado para simular falha do BRAPI em 1 usuário
--   Fundo 3 (usuario 3 / outro): MXRF11
--
-- O job itera sobre os 3 usuários ativos. Cada teste mocka o BrapiClient para
-- retornar ou lançar exception conforme o cenário validado.
-- ============================================

INSERT INTO usuario (
    nome, email, senha, perfil, ativo, data_criacao, data_atualizacao
) VALUES
    ('Admin FIIs',    'admin@fiis.com', 'placeholder-nao-usado', 'ADMIN', TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    ('Outro Usuário', 'outro@fiis.com', 'placeholder-nao-usado', 'USER',  TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO fundo (
    usuario_id, ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    (1, 'HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    (2, 'FAIL11', 'Fail Tester FII',    '00000000000191', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    (3, 'MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

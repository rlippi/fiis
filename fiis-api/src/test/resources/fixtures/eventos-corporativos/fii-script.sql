-- ============================================
-- FII-SCRIPT — Massa para testes do EventoCorporativoController
-- ============================================
-- Executado APÓS o setup.sql (TRUNCATE + RESTART IDENTITY + usuário de teste ID=1).
--
-- 2 fundos e 2 eventos (todos do usuário de teste):
--   ID 1 - HGLG11: DESDOBRAMENTO 1:2 em 2026-04-12
--   ID 2 - HGLG11: BONIFICACAO 10% em 2026-04-15
-- ============================================

INSERT INTO fundo (
    usuario_id, ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    (1, 'HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    (1, 'MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO evento_corporativo (
    usuario_id, fundo_id, tipo, data, fator, descricao, data_criacao, data_atualizacao
) VALUES
    (1, 1, 'DESDOBRAMENTO', '2026-04-12', 2.000000, 'Desdobramento 1:2',           '2026-04-12 10:00:00', '2026-04-12 10:00:00'),
    (1, 1, 'BONIFICACAO',   '2026-04-15', 0.100000, 'Bonificação de 10% em cotas', '2026-04-15 10:00:00', '2026-04-15 10:00:00');

-- ============================================
-- FII-SCRIPT — Massa para testes do ProventoController
-- ============================================
-- 2 fundos e 3 proventos em datas diferentes:
--   ID 1 - HGLG11: RENDIMENTO R$ 1,10 × 10 cotas (ref 2026-02-28, pag 2026-03-15)
--   ID 2 - HGLG11: RENDIMENTO R$ 1,12 × 12 cotas (ref 2026-03-31, pag 2026-04-15)
--   ID 3 - MXRF11: RENDIMENTO R$ 0,10 × 100 cotas (ref 2026-03-31, pag 2026-04-15)
-- ============================================

INSERT INTO fundo (
    ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    ('HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    ('MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO provento (
    fundo_id, tipo_provento, data_referencia, data_pagamento, valor_por_cota, quantidade_cotas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 'RENDIMENTO', '2026-02-28', '2026-03-15', 1.100000,  10, 'Rendimento fevereiro', '2026-03-15 10:00:00', '2026-03-15 10:00:00'),
    (1, 'RENDIMENTO', '2026-03-31', '2026-04-15', 1.120000,  12, 'Rendimento março',     '2026-04-15 10:00:00', '2026-04-15 10:00:00'),
    (2, 'RENDIMENTO', '2026-03-31', '2026-04-15', 0.100000, 100, 'Rendimento mensal',    '2026-04-15 10:00:00', '2026-04-15 10:00:00');

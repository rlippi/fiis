-- ============================================
-- FII-SCRIPT — Massa para testes do PosicaoController
-- ============================================
-- Cenário realista para validar os cálculos de posição:
--
--   Fundos ativos:
--     ID 1 - HGLG11 - com operações, proventos e cotação
--     ID 2 - MXRF11 - sem operações (posição zerada)
--
--   Operações do HGLG11:
--     10 cotas a R$ 150,25 + R$ 0,50 = gastos R$ 1.503,00  (PM parcial R$ 150,30)
--      5 cotas a R$ 155,00 + R$ 0,50 = gastos R$ 775,50    (PM    R$ 151,90)
--     -3 cotas a R$ 160,00 − R$ 0,30 = recebeu R$ 479,70   (Lucro R$ 24,00)
--     Posição final: 12 cotas, PM R$ 151,90, custo R$ 1.822,80
--
--   Provento do HGLG11: 1,10/cota × 12 cotas = R$ 13,20
--
--   Cotação do HGLG11 em 2026-04-17: R$ 158,50
--     → valorAtual = 12 × 158,50 = R$ 1.902,00
--     → variação = (158,50 − 151,90) / 151,90 × 100 = 4,34%
--     → yieldSobreCusto = 13,20 / 1.822,80 × 100 = 0,72%
--     → rentabilidade total = (1902 + 479,70 + 13,20 − 2278,50) / 2278,50 × 100 = 5,11%
-- ============================================

INSERT INTO fundo (
    ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    ('HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    ('MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO operacao (
    fundo_id, tipo, data_operacao, quantidade, preco_unitario, taxas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 'COMPRA', '2026-03-15', 10, 150.25, 0.50, NULL, '2026-03-15 10:00:00', '2026-03-15 10:00:00'),
    (1, 'COMPRA', '2026-03-20',  5, 155.00, 0.50, NULL, '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
    (1, 'VENDA',  '2026-04-01',  3, 160.00, 0.30, NULL, '2026-04-01 10:00:00', '2026-04-01 10:00:00');

INSERT INTO provento (
    fundo_id, tipo_provento, data_referencia, data_pagamento, valor_por_cota, quantidade_cotas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 'RENDIMENTO', '2026-03-31', '2026-04-15', 1.100000, 12, NULL, '2026-04-15 10:00:00', '2026-04-15 10:00:00');

INSERT INTO cotacao (
    fundo_id, data, preco_fechamento, preco_abertura, preco_minimo, preco_maximo, volume, data_criacao, data_atualizacao
) VALUES
    (1, '2026-04-17', 158.5000, NULL, NULL, NULL, NULL, '2026-04-17 18:00:00', '2026-04-17 18:00:00');

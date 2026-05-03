-- ============================================
-- FII-SCRIPT — Massa para testes do RelatorioController
-- ============================================
-- Executado APÓS o setup.sql (TRUNCATE + RESTART IDENTITY + usuário de teste ID=1).
--
-- Cenário com 2 fundos ativos, operações, proventos em 2 meses distintos
-- e cotações (necessárias para alocação com valor atual > 0). Todos do usuário de teste:
--
--   HGLG11 (TIJOLO/LOGISTICA):
--     Operações: +10 @ 150,25 + 0,50 = 1.503,00
--                +5  @ 155,00 + 0,50 =   775,50
--                -3  @ 160,00 - 0,30 =   479,70
--     Posição: 12 cotas, PM R$ 151,90, custo R$ 1.822,80
--     Provento: 1,10 × 12 = R$ 13,20 pago em 2026-04-15
--
--   MXRF11 (PAPEL/RECEBIVEIS):
--     Operações: +100 @ 10,00 + 0,30 = 1.000,30
--     Posição: 100 cotas, PM R$ 10,0030, custo R$ 1.000,30
--     Provento: 0,10 × 100 = R$ 10,00 pago em 2026-03-15
--     Provento: 0,12 × 100 = R$ 12,00 pago em 2026-04-15
-- ============================================

INSERT INTO fundo (
    usuario_id, ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    (1, 'HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    (1, 'MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO operacao (
    usuario_id, fundo_id, tipo, data_operacao, quantidade, preco_unitario, taxas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 1, 'COMPRA', '2026-03-15', 10, 150.25, 0.50, NULL, '2026-03-15 10:00:00', '2026-03-15 10:00:00'),
    (1, 1, 'COMPRA', '2026-03-20',  5, 155.00, 0.50, NULL, '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
    (1, 1, 'VENDA',  '2026-04-01',  3, 160.00, 0.30, NULL, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
    (1, 2, 'COMPRA', '2026-02-10', 100, 10.00, 0.30, NULL, '2026-02-10 10:00:00', '2026-02-10 10:00:00');

INSERT INTO provento (
    usuario_id, fundo_id, tipo_provento, data_referencia, data_pagamento, valor_por_cota, quantidade_cotas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 1, 'RENDIMENTO', '2026-03-31', '2026-04-15', 1.100000,  12, NULL, '2026-04-15 10:00:00', '2026-04-15 10:00:00'),
    (1, 2, 'RENDIMENTO', '2026-02-28', '2026-03-15', 0.100000, 100, NULL, '2026-03-15 10:00:00', '2026-03-15 10:00:00'),
    (1, 2, 'RENDIMENTO', '2026-03-31', '2026-04-15', 0.120000, 100, NULL, '2026-04-15 10:00:00', '2026-04-15 10:00:00');

INSERT INTO cotacao (
    usuario_id, fundo_id, data, preco_fechamento, preco_abertura, preco_minimo, preco_maximo, volume, data_criacao, data_atualizacao
) VALUES
    (1, 1, '2026-04-17', 158.5000, NULL, NULL, NULL, NULL, '2026-04-17 18:00:00', '2026-04-17 18:00:00'),
    (1, 2, '2026-04-17',  10.5000, NULL, NULL, NULL, NULL, '2026-04-17 18:00:00', '2026-04-17 18:00:00');

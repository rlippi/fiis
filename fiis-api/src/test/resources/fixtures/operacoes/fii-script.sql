-- ============================================
-- FII-SCRIPT — Massa de dados para testes do OperacaoController
-- ============================================
-- Executado APÓS o setup.sql (TRUNCATE + RESTART IDENTITY).
--
-- Insere 2 fundos e 3 operações iniciais cobrindo os cenários:
--   Fundos:
--     ID 1 - HGLG11 (ativo) - usado na maioria dos cenários
--     ID 2 - MXRF11 (ativo) - sem operações (para testar lista vazia)
--   Operações (do HGLG11):
--     ID 1 - COMPRA  10 cotas a 150,25 em 2026-03-15
--     ID 2 - COMPRA   5 cotas a 155,00 em 2026-03-20
--     ID 3 - VENDA    3 cotas a 160,00 em 2026-04-01
--   Posição atual em HGLG11: 12 cotas
-- ============================================

INSERT INTO fundo (
    ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    ('HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    ('MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

INSERT INTO operacao (
    fundo_id, tipo, data_operacao, quantidade, preco_unitario, taxas, observacao, data_criacao, data_atualizacao
) VALUES
    (1, 'COMPRA', '2026-03-15', 10, 150.25, 0.50, 'Primeira compra',  '2026-03-15 10:00:00', '2026-03-15 10:00:00'),
    (1, 'COMPRA', '2026-03-20',  5, 155.00, 0.50, 'Segunda compra',   '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
    (1, 'VENDA',  '2026-04-01',  3, 160.00, 0.30, NULL,               '2026-04-01 10:00:00', '2026-04-01 10:00:00');

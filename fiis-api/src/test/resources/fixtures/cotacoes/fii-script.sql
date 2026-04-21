-- ============================================
-- FII-SCRIPT — Massa para testes do CotacaoController
-- ============================================
-- Executado APÓS o setup.sql (TRUNCATE + RESTART IDENTITY + usuário de teste ID=1).
--
-- 3 fundos e 3 cotações (todos do usuário de teste):
--   Fundos:
--     ID 1 - HGLG11 (com 2 cotações)
--     ID 2 - MXRF11 (com 1 cotação)
--     ID 3 - VISC11 (SEM cotações) — para cenário "última sem cotação"
--
--   Cotações:
--     ID 1 - HGLG11 em 2026-04-16: R$ 158,00
--     ID 2 - HGLG11 em 2026-04-17: R$ 158,50 (mais recente)
--     ID 3 - MXRF11 em 2026-04-17: R$ 10,25
-- ============================================

INSERT INTO fundo (
    usuario_id, ticker, nome, cnpj, tipo, segmento, ativo, data_criacao, data_atualizacao
) VALUES
    (1, 'HGLG11', 'CSHG Logística FII', '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    (1, 'MXRF11', 'Maxi Renda FII',     '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
    (1, 'VISC11', 'Vinci Shopping FII', '17554274000125', 'TIJOLO', 'SHOPPING',   TRUE, '2026-01-03 10:00:00', '2026-01-03 10:00:00');

INSERT INTO cotacao (
    usuario_id, fundo_id, data, preco_fechamento, preco_abertura, preco_minimo, preco_maximo, volume, data_criacao, data_atualizacao
) VALUES
    (1, 1, '2026-04-16', 158.0000, 157.5000, 157.0000, 158.5000, 1500000.00, '2026-04-16 18:00:00', '2026-04-16 18:00:00'),
    (1, 1, '2026-04-17', 158.5000, 158.0000, 156.8000, 159.2000, 2450000.00, '2026-04-17 18:00:00', '2026-04-17 18:00:00'),
    (1, 2, '2026-04-17',  10.2500,   NULL,     NULL,     NULL,        NULL,  '2026-04-17 18:00:00', '2026-04-17 18:00:00');

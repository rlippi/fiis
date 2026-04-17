-- ============================================
-- FII-SCRIPT — Massa de dados para testes do FundoController
-- ============================================
-- Executado APÓS o setup.sql (que fez TRUNCATE + RESTART IDENTITY),
-- então os IDs a seguir começam em 1 e são previsíveis.
--
-- Insere 3 fundos cobrindo os cenários principais:
--   ID 1 - HGLG11 (ativo, Tijolo/Logística)
--   ID 2 - MXRF11 (ativo, Papel/Recebíveis)
--   ID 3 - VISC11 (INATIVO, Tijolo/Shopping) — usado para testar filtro ativo
-- ============================================

INSERT INTO fundo (
    ticker,
    nome,
    cnpj,
    tipo,
    segmento,
    ativo,
    data_criacao,
    data_atualizacao
) VALUES
    ('HGLG11', 'CSHG Logística FII',  '11728688000147', 'TIJOLO', 'LOGISTICA',  TRUE,  '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
    ('MXRF11', 'Maxi Renda FII',      '97521225000140', 'PAPEL',  'RECEBIVEIS', TRUE,  '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
    ('VISC11', 'Vinci Shopping FII',  '17554274000125', 'TIJOLO', 'SHOPPING',   FALSE, '2026-01-03 10:00:00', '2026-01-03 10:00:00');

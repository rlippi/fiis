-- ============================================
-- Desativa todos os fundos da massa de teste.
-- Usado no cenário "carteira sem fundos ativos" do importar-brapi.
-- ============================================
UPDATE fundo SET ativo = FALSE;

package com.renlip.fiis.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projeção mínima da última cotação de um fundo (data + preço de fechamento).
 *
 * <p>Existe especificamente para servir como valor cacheável: ao retornar este
 * record em vez da entidade {@code Cotacao}, evitamos {@code LazyInitializationException}
 * quando o cache devolve um valor fora da sessão Hibernate que o originou.
 * Records são imutáveis, thread-safe e contêm apenas tipos primitivos/imutáveis,
 * o que os torna seguros para qualquer cache local.</p>
 */
public record UltimaCotacaoResumo(

    LocalDate data,
    BigDecimal precoFechamento
) {
}

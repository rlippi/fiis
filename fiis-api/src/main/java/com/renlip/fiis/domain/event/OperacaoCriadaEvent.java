package com.renlip.fiis.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.renlip.fiis.domain.enumeration.TipoOperacao;

/**
 * Fato de domínio publicado quando uma {@code Operacao} é criada com sucesso.
 *
 * <p>Carrega um <i>snapshot</i> dos campos relevantes em vez da entidade JPA
 * para que listeners possam ser executados em fases pós-commit sem risco de
 * {@code LazyInitializationException} ou de leitura inconsistente.</p>
 *
 * @param operacaoId             ID da operação recém-criada
 * @param fundoId                ID do fundo associado
 * @param ticker                 código do fundo (ex: {@code HGLG11}) — útil para logs legíveis
 * @param tipo                   {@link TipoOperacao#COMPRA} ou {@link TipoOperacao#VENDA}
 * @param quantidade             cotas negociadas
 * @param precoUnitario          preço unitário praticado
 * @param dataOperacao           data em que a operação ocorreu
 * @param usuarioDonoId          ID do usuário dono do fundo (=> dono da operação)
 * @param usuarioRequisitanteId  ID do usuário autenticado que disparou a ação;
 *                               diferente de {@code usuarioDonoId} apenas quando
 *                               um ADMIN atua sobre carteira alheia
 */
public record OperacaoCriadaEvent(

    Long operacaoId,
    Long fundoId,
    String ticker,
    TipoOperacao tipo,
    Integer quantidade,
    BigDecimal precoUnitario,
    LocalDate dataOperacao,
    Long usuarioDonoId,
    Long usuarioRequisitanteId
) {
}

package com.renlip.fiis.domain.event;

/**
 * Fato de domínio publicado quando uma {@code Operacao} é excluída.
 *
 * <p>Carrega apenas o necessário para rastreabilidade — o registro já não
 * existe mais no banco no momento em que listeners {@code AFTER_COMMIT}
 * processam o evento.</p>
 *
 * @param operacaoId             ID da operação excluída
 * @param fundoId                ID do fundo associado (no momento da exclusão)
 * @param ticker                 código do fundo, para legibilidade no log
 * @param usuarioDonoId          dono original da operação
 * @param usuarioRequisitanteId  quem disparou a exclusão
 */
public record OperacaoExcluidaEvent(

    Long operacaoId,
    Long fundoId,
    String ticker,
    Long usuarioDonoId,
    Long usuarioRequisitanteId
) {
}

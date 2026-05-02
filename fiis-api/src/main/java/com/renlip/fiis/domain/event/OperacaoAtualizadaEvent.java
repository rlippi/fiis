package com.renlip.fiis.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.renlip.fiis.domain.enumeration.TipoOperacao;

/**
 * Fato de domínio publicado quando uma {@code Operacao} é atualizada com sucesso.
 *
 * <p>Carrega o estado <b>após</b> a alteração. Versão "antes" do registro não é
 * incluída no evento — para gerar diff em auditoria, esta seria uma extensão
 * futura (provavelmente com um sub-record adicional).</p>
 *
 * @param operacaoId             ID da operação editada
 * @param fundoId                ID do fundo (potencialmente novo, se a edição mudou de fundo)
 * @param ticker                 código do novo fundo
 * @param tipo                   {@link TipoOperacao} pós-edição
 * @param quantidade             quantidade pós-edição
 * @param precoUnitario          preço pós-edição
 * @param dataOperacao           data pós-edição
 * @param usuarioDonoId          dono da operação após a edição
 * @param usuarioRequisitanteId  quem disparou a edição (admin pode editar carteira alheia)
 */
public record OperacaoAtualizadaEvent(

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

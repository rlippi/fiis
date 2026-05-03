/**
 * O pacote {@code com.renlip.fiis.domain.dto.brapi} contém os DTOs que refletem
 * a estrutura de resposta da BRAPI (https://brapi.dev), consumida pela
 * aplicação para importar cotações de FIIs do mercado. Esses tipos ficam
 * isolados do resto dos DTOs para deixar claro que representam um contrato
 * externo, não nosso — e portanto usam {@code @JsonIgnoreProperties(ignoreUnknown = true)}
 * para tolerar a evolução da API.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://brapi.dev/docs">BRAPI | Documentação</a>
 */
package com.renlip.fiis.domain.dto.brapi;

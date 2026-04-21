package com.renlip.fiis.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.renlip.fiis.config.BrapiProperties;
import com.renlip.fiis.domain.dto.brapi.BrapiQuote;
import com.renlip.fiis.domain.dto.brapi.BrapiQuoteResponse;

/**
 * Cliente HTTP para a BRAPI (https://brapi.dev).
 *
 * <p>Encapsula o {@link RestClient} do Spring e expõe um método por endpoint
 * consumido. Mantém o restante da aplicação alheio aos detalhes de protocolo
 * (URL, serialização, token query string) — services e controllers dependem
 * apenas desta classe.</p>
 *
 * <p>O {@code RestClient} é construído uma vez no construtor, com a URL base
 * vinda de {@link BrapiProperties}. O token é anexado como query parameter
 * quando presente, seguindo a convenção oficial da BRAPI.</p>
 *
 * <p><b>Free tier:</b> o plano gratuito da BRAPI permite apenas 1 ativo por
 * requisição. Para suportar carteiras com múltiplos fundos, {@link #buscarCotacoes(List)}
 * faz uma chamada por ticker em loop e consolida o resultado. Planos pagos
 * aceitariam múltiplos tickers separados por vírgula numa única chamada, mas
 * a implementação atual prioriza o caso grátis.</p>
 */
@Component
public class BrapiClient {

    private final BrapiProperties properties;

    private final RestClient restClient;

    public BrapiClient(final BrapiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
            .baseUrl(properties.url())
            .build();
    }

    /**
     * Busca na BRAPI a cotação atual de múltiplos tickers.
     *
     * <p>Itera sobre a lista e faz uma requisição por ticker (exigência do
     * plano gratuito). Tickers que a BRAPI não reconhece (HTTP 404) são
     * ignorados silenciosamente — o serviço chamador reporta esses casos em
     * {@code naoEncontradosBrapi}. Qualquer outra falha (5xx, timeout,
     * conexão recusada) propaga como {@code RestClientException}.</p>
     *
     * @param tickers lista de códigos de negociação (ex: {@code ["HGLG11", "KNCR11"]})
     * @return envelope com as cotações encontradas (subset dos tickers solicitados)
     */
    public BrapiQuoteResponse buscarCotacoes(final List<String> tickers) {
        List<BrapiQuote> encontradas = new ArrayList<>();

        for (String ticker : tickers) {
            try {
                BrapiQuoteResponse resposta = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/quote/{ticker}");
                        if (StringUtils.hasText(properties.token())) {
                            uriBuilder.queryParam("token", properties.token());
                        }
                        return uriBuilder.build(ticker);
                    })
                    .retrieve()
                    .body(BrapiQuoteResponse.class);

                if (resposta != null && resposta.results() != null) {
                    encontradas.addAll(resposta.results());
                }
            } catch (HttpClientErrorException.NotFound ex) {
                // Ticker inexistente na BRAPI — ignora; o serviço tratará como
                // "não encontrado" ao comparar a lista consolidada com a original.
            }
        }

        return new BrapiQuoteResponse(encontradas);
    }
}

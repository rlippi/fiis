package com.renlip.fiis.domain.enumeration;

import lombok.Getter;

/**
 * Mensagens padronizadas da aplicação, compostas pela chave para o texto contido no arquivo
 * {@code messages_pt_BR.properties} e pelo código associado. O código é composto pelo acrônimo
 * do projeto (3 caracteres — "FII") mais uma sequência numérica de 4 dígitos, permitindo aos
 * consumidores da API customizarem a mensagem para seus usuários a partir do código, uma vez
 * que o texto no arquivo de propriedades pode ser alterado sem aviso prévio.
 *
 * <p><strong>Importante:</strong> todas as mensagens devem ser mantidas em ordem alfabética para
 * facilitar a manutenção do código e evitar duplicidade.</p>
 */
@Getter
public enum MensagemEnum {

    ACESSO_NEGADO("com.renlip.fiis.ACESSO_NEGADO", "FII0015"),
    AUTENTICACAO_REQUERIDA("com.renlip.fiis.AUTENTICACAO_REQUERIDA", "FII0016"),
    CARTEIRA_SEM_FUNDOS_ATIVOS("com.renlip.fiis.CARTEIRA_SEM_FUNDOS_ATIVOS", "FII0019"),
    COTACAO_BRAPI_INDISPONIVEL("com.renlip.fiis.COTACAO_BRAPI_INDISPONIVEL", "FII0020"),
    COTACAO_INTERVALO_PRECO_INVALIDO("com.renlip.fiis.COTACAO_INTERVALO_PRECO_INVALIDO", "FII0001"),
    COTACAO_JA_EXISTE_NO_PERIODO("com.renlip.fiis.COTACAO_JA_EXISTE_NO_PERIODO", "FII0002"),
    COTACAO_NAO_ENCONTRADA("com.renlip.fiis.COTACAO_NAO_ENCONTRADA", "FII0003"),
    CREDENCIAIS_INVALIDAS("com.renlip.fiis.CREDENCIAIS_INVALIDAS", "FII0017"),
    EMAIL_JA_CADASTRADO("com.renlip.fiis.EMAIL_JA_CADASTRADO", "FII0021"),
    ERRO_INESPERADO("com.renlip.fiis.ERRO_INESPERADO", "FII0004"),
    ERRO_VALIDACAO_CAMPOS("com.renlip.fiis.ERRO_VALIDACAO_CAMPOS", "FII0005"),
    EVENTO_CORPORATIVO_NAO_ENCONTRADO("com.renlip.fiis.EVENTO_CORPORATIVO_NAO_ENCONTRADO", "FII0006"),
    FUNDO_NAO_ENCONTRADO("com.renlip.fiis.FUNDO_NAO_ENCONTRADO", "FII0007"),
    FUNDO_TICKER_JA_CADASTRADO("com.renlip.fiis.FUNDO_TICKER_JA_CADASTRADO", "FII0008"),
    OPERACAO_EDICAO_POSICAO_NEGATIVA("com.renlip.fiis.OPERACAO_EDICAO_POSICAO_NEGATIVA", "FII0009"),
    OPERACAO_NAO_ENCONTRADA("com.renlip.fiis.OPERACAO_NAO_ENCONTRADA", "FII0010"),
    OPERACAO_VENDA_EXCEDE_POSICAO("com.renlip.fiis.OPERACAO_VENDA_EXCEDE_POSICAO", "FII0011"),
    PERIODO_INVALIDO("com.renlip.fiis.PERIODO_INVALIDO", "FII0012"),
    PROVENTO_DATA_PAGAMENTO_ANTERIOR_REFERENCIA("com.renlip.fiis.PROVENTO_DATA_PAGAMENTO_ANTERIOR_REFERENCIA", "FII0013"),
    PROVENTO_NAO_ENCONTRADO("com.renlip.fiis.PROVENTO_NAO_ENCONTRADO", "FII0014"),
    ROTA_NAO_ENCONTRADA("com.renlip.fiis.ROTA_NAO_ENCONTRADA", "FII0018");

    private final String codigo;

    private final String texto;

    MensagemEnum(final String texto, final String codigo) {
        this.texto = texto;
        this.codigo = codigo;
    }
}

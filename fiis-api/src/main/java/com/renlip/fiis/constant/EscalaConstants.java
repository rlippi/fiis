package com.renlip.fiis.constant;

/**
 * Escalas numéricas padrão utilizadas nos cálculos financeiros da aplicação.
 *
 * <p>Centraliza as escalas de arredondamento para garantir consistência entre
 * services que manipulam {@link java.math.BigDecimal}.</p>
 */
public final class EscalaConstants {

    /** Escala utilizada em cálculos intermediários (ex: preço médio). */
    public static final int ESCALA_CALCULO = 6;

    /** Escala utilizada nos valores monetários finais expostos ao cliente. */
    public static final int ESCALA_MONETARIA = 2;

    private EscalaConstants() {
    }
}

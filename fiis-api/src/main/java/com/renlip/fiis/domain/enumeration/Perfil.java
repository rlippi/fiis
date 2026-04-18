package com.renlip.fiis.domain.enumeration;

/**
 * Perfil de acesso do usuário no sistema.
 *
 * <p>Usado pelo Spring Security para decidir quais rotas cada usuário pode acessar.</p>
 */
public enum Perfil {

    /**
     * Usuário comum — pode gerenciar sua carteira (fundos, operações, proventos, etc.).
     */
    USER("Usuário"),

    /**
     * Administrador — acesso irrestrito; reservado para operações de gestão
     * (ex: gerenciar outros usuários no futuro).
     */
    ADMIN("Administrador");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do perfil (para exibição).
     *
     * @return descrição legível (ex: "Usuário", "Administrador")
     */
    public String getDescricao() {
        return descricao;
    }
}

/**
 * O pacote {@code com.renlip.fiis.domain.dto} contém os Data Transfer Objects de saída
 * (Response) da API, representando os dados que os endpoints devolvem ao consumidor.
 * São implementados como {@code record} imutáveis e tipicamente populados via mapper a
 * partir de uma entidade JPA. A separação entity/DTO evita expor detalhes do modelo
 * persistente para fora da aplicação.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://martinfowler.com/eaaCatalog/dataTransferObject.html">Martin Fowler | DTO</a>
 * @see <a href="https://docs.oracle.com/en/java/javase/21/language/records.html">Java 21 | Records</a>
 */
package com.renlip.fiis.domain.dto;

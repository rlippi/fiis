/**
 * O pacote {@code com.renlip.fiis.domain.mapper} contém os mappers responsáveis por
 * converter entre entidades JPA e DTOs/VOs, gerados em tempo de compilação pelo
 * MapStruct. As interfaces/classes abstratas aqui são anotadas com
 * {@code @Mapper(componentModel = "spring")}, permitindo injeção por construtor nos
 * services, e usam {@code uses = ...} para delegar conversões aninhadas.
 *
 * @author Renato Lippi
 * @since 1.0.0
 * @see <a href="https://mapstruct.org/documentation/reference-guide/">MapStruct | Reference Guide</a>
 */
package com.renlip.fiis.domain.mapper;

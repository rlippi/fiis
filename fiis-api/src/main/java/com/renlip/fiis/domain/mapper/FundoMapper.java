package com.renlip.fiis.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import com.renlip.fiis.domain.dto.FundoResponse;
import com.renlip.fiis.domain.entity.Fundo;

/**
 * Mapper entre {@link Fundo} e {@link FundoResponse}, enriquecendo os enums
 * {@code tipo} e {@code segmento} com suas descrições amigáveis.
 */
@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public abstract class FundoMapper {

    @Mapping(target = "tipoDescricao",
        expression = "java(entity.getTipo() == null ? null : entity.getTipo().getDescricao())")
    @Mapping(target = "segmentoDescricao",
        expression = "java(entity.getSegmento() == null ? null : entity.getSegmento().getDescricao())")
    public abstract FundoResponse toResponse(Fundo entity);

    public abstract List<FundoResponse> toResponseList(List<Fundo> entities);
}

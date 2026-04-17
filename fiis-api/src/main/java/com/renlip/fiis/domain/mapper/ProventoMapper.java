package com.renlip.fiis.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import com.renlip.fiis.domain.dto.ProventoResponse;
import com.renlip.fiis.domain.entity.Provento;

/**
 * Mapper entre {@link Provento} e {@link ProventoResponse}, populando
 * {@code tipoProventoDescricao} e o campo calculado {@code valorTotal}.
 */
@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
    uses = FundoResumoMapper.class)
public abstract class ProventoMapper {

    @Mapping(target = "tipoProventoDescricao",
        expression = "java(entity.getTipoProvento() == null ? null : entity.getTipoProvento().getDescricao())")
    @Mapping(target = "valorTotal", expression = "java(entity.calcularValorTotal())")
    public abstract ProventoResponse toResponse(Provento entity);

    public abstract List<ProventoResponse> toResponseList(List<Provento> entities);
}

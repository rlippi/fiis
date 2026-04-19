package com.renlip.fiis.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import com.renlip.fiis.domain.dto.EventoCorporativoResponse;
import com.renlip.fiis.domain.entity.EventoCorporativo;

/**
 * Mapper entre {@link EventoCorporativo} e {@link EventoCorporativoResponse},
 * populando {@code tipoDescricao} a partir do enum.
 */
@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
    uses = FundoResumoMapper.class)
public abstract class EventoCorporativoMapper {

    @Mapping(target = "tipoDescricao",
        expression = "java(entity.getTipo() == null ? null : entity.getTipo().getDescricao())")
    public abstract EventoCorporativoResponse toResponse(EventoCorporativo entity);

    public abstract List<EventoCorporativoResponse> toResponseList(List<EventoCorporativo> entities);
}

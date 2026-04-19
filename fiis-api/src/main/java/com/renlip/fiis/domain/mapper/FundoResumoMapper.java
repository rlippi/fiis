package com.renlip.fiis.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

import com.renlip.fiis.domain.dto.FundoResumoResponse;
import com.renlip.fiis.domain.entity.Fundo;

/**
 * Mapper entre {@link Fundo} e sua versão resumida {@link FundoResumoResponse}.
 *
 * <p>Usado por outros mappers ({@code uses}) quando um DTO precisa embutir o resumo do fundo.</p>
 */
@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public abstract class FundoResumoMapper {

    public abstract FundoResumoResponse toResponse(Fundo entity);

    public abstract List<FundoResumoResponse> toResponseList(List<Fundo> entities);
}

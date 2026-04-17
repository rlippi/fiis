package com.renlip.fiis.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

import com.renlip.fiis.domain.dto.CotacaoResponse;
import com.renlip.fiis.domain.entity.Cotacao;

/**
 * Mapper entre {@link Cotacao} e {@link CotacaoResponse}.
 *
 * <p>Delega a conversão do fundo a {@link FundoResumoMapper} via {@code uses}.</p>
 */
@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
    uses = FundoResumoMapper.class)
public abstract class CotacaoMapper {

    public abstract CotacaoResponse toResponse(Cotacao entity);

    public abstract List<CotacaoResponse> toResponseList(List<Cotacao> entities);
}

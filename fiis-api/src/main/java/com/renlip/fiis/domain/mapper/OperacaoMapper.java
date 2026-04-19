package com.renlip.fiis.domain.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import com.renlip.fiis.domain.dto.OperacaoResponse;
import com.renlip.fiis.domain.entity.Operacao;

/**
 * Mapper entre {@link Operacao} e {@link OperacaoResponse}, populando
 * {@code tipoDescricao} e o campo calculado {@code valorTotal}.
 */
@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
    uses = FundoResumoMapper.class)
public abstract class OperacaoMapper {

    @Mapping(target = "tipoDescricao",
        expression = "java(entity.getTipo() == null ? null : entity.getTipo().getDescricao())")
    @Mapping(target = "valorTotal", expression = "java(entity.calcularValorTotal())")
    public abstract OperacaoResponse toResponse(Operacao entity);

    public abstract List<OperacaoResponse> toResponseList(List<Operacao> entities);
}

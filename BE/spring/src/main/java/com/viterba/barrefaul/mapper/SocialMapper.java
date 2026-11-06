package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.dto.SocialResponse;
import com.viterba.barrefaul.entity.Social;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SocialMapper {

    @Mapping(target = "id", ignore = true)
    Social toEntity(SocialRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(SocialRequest request, @MappingTarget Social social);

    SocialResponse toResponse(Social social);

}
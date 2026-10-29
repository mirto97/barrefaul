package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.SocialRequest;
import com.viterba.barrefaul.dto.SocialResponse;
import com.viterba.barrefaul.entity.Social;

@Mapper(componentModel = "spring")
public interface SocialMapper {

    @Mapping(target = "id", ignore = true)
    Social toEntity(SocialRequest request);

    SocialResponse toResponse(Social social);

}
package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.entity.Freestyler;

@Mapper(componentModel = "spring", uses = SocialMapper.class)
public interface FreestylerMapper {

    @Mapping(target = "id", ignore = true)
    Freestyler toEntity(FreestylerRequest request);

    // Il ranking non è nell'entity: è calcolato dal service e passato qui
    @Mapping(target = "ranking", source = "ranking")
    FreestylerResponse toResponse(Freestyler freestyler, Integer ranking);

}
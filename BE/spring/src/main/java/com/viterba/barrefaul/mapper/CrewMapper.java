package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.dto.CrewSummary;
import com.viterba.barrefaul.entity.Crew;

@Mapper(componentModel = "spring", uses = SocialMapper.class)
public interface CrewMapper {

    @Mapping(target = "id", ignore = true)
    Crew toEntity(CrewRequest request);

    CrewResponse toResponse(Crew crew);

    CrewSummary toSummary(Crew crew);

}
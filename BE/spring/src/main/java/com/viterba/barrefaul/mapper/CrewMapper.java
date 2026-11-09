package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.viterba.barrefaul.dto.CrewRequest;
import com.viterba.barrefaul.dto.CrewResponse;
import com.viterba.barrefaul.dto.CrewSummary;
import com.viterba.barrefaul.entity.Crew;

@Mapper(componentModel = "spring", 
    uses = SocialMapper.class, 
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    // Una lista null in ingresso diventa vuota, così le collezioni dell'entity non finiscono a null
    nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface CrewMapper {

    @Mapping(target = "id", ignore = true)
    Crew toEntity(CrewRequest request);

    CrewResponse toResponse(Crew crew);

    CrewSummary toSummary(Crew crew);

    // così se roles è null nella request, le collezioni esistenti restano invariate
    // I socials non si mappano qui: il merge per id è un bean apposito
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "socials", ignore = true)
    void updateEntity(CrewRequest request, @MappingTarget Crew crew);

}
package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.viterba.barrefaul.dto.FreestylerRequest;
import com.viterba.barrefaul.dto.FreestylerResponse;
import com.viterba.barrefaul.entity.Freestyler;

@Mapper(componentModel = "spring", uses = SocialMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FreestylerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "participations", ignore = true)
    @Mapping(target = "firstPlaces", ignore = true)
    @Mapping(target = "secondPlaces", ignore = true)
    @Mapping(target = "semifinals", ignore = true)
    @Mapping(target = "quarterfinals", ignore = true)
    @Mapping(target = "roundOf16", ignore = true)
    @Mapping(target = "selections", ignore = true)
    @Mapping(target = "jury", ignore = true)
    Freestyler toEntity(FreestylerRequest request);

    // Il ranking non è nell'entity: è calcolato dal service e passato qui
    @Mapping(target = "ranking", source = "ranking")
    FreestylerResponse toResponse(Freestyler freestyler, Integer ranking);

    // I socials non si mappano qui: il merge per id è nel SocialMerger
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "socials", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "participations", ignore = true)
    @Mapping(target = "firstPlaces", ignore = true)
    @Mapping(target = "secondPlaces", ignore = true)
    @Mapping(target = "semifinals", ignore = true)
    @Mapping(target = "quarterfinals", ignore = true)
    @Mapping(target = "roundOf16", ignore = true)
    @Mapping(target = "selections", ignore = true)
    @Mapping(target = "jury", ignore = true)
    @Mapping(target = "roles", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FreestylerRequest request, @MappingTarget Freestyler freestyler);

}
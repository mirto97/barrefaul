package com.viterba.barrefaul.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;

@Mapper(componentModel = "spring", uses = CrewMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    Event toEntity(EventRequest request);

    EventResponse toResponse(Event event);

    // Se judges o hosts sono null nella request, le collezioni esistenti restano invariate
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "judges", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "hosts", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EventRequest request, @MappingTarget Event event);

}
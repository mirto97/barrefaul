package com.viterba.barrefaul.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.viterba.barrefaul.dto.EventRequest;
import com.viterba.barrefaul.dto.EventResponse;
import com.viterba.barrefaul.entity.Event;

@Mapper(componentModel = "spring", uses = CrewMapper.class)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "judges", ignore = true)
    @Mapping(target = "hosts", ignore = true)
    Event toEntity(EventRequest request);

    EventResponse toResponse(Event event);

}
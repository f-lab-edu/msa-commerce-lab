package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;
import com.msa.commerce.orchestrator.domain.crossdomain.DomainType;
import com.msa.commerce.orchestrator.domain.crossdomain.EntityType;
import com.msa.commerce.orchestrator.domain.crossdomain.EventType;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CrossDomainEventMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(source = "eventType", target = "eventType", qualifiedByName = "eventTypeToString")
    @Mapping(source = "sourceDomain", target = "sourceDomain", qualifiedByName = "domainTypeToString")
    @Mapping(source = "targetDomains", target = "targetDomainsJson", qualifiedByName = "domainListToJson")
    @Mapping(source = "entityType", target = "entityType", qualifiedByName = "entityTypeToString")
    CrossDomainEventJpaEntity toEntity(CrossDomainEvent domain);

    @Mapping(source = "eventType", target = "eventType", qualifiedByName = "stringToEventType")
    @Mapping(source = "sourceDomain", target = "sourceDomain", qualifiedByName = "stringToDomainType")
    @Mapping(source = "targetDomainsJson", target = "targetDomains", qualifiedByName = "jsonToDomainList")
    @Mapping(source = "entityType", target = "entityType", qualifiedByName = "stringToEntityType")
    CrossDomainEvent toDomain(CrossDomainEventJpaEntity entity);

    @Named("eventTypeToString")
    default String eventTypeToString(EventType eventType) {
        return eventType != null ? eventType.name() : null;
    }

    @Named("stringToEventType")
    default EventType stringToEventType(String eventType) {
        if (eventType == null) {
            return null;
        }
        try {
            return EventType.valueOf(eventType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }

    @Named("domainTypeToString")
    default String domainTypeToString(DomainType domainType) {
        return domainType != null ? domainType.name() : null;
    }

    @Named("stringToDomainType")
    default DomainType stringToDomainType(String domainType) {
        if (domainType == null) {
            return null;
        }
        try {
            return DomainType.valueOf(domainType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown domain type: " + domainType);
        }
    }

    @Named("entityTypeToString")
    default String entityTypeToString(EntityType entityType) {
        return entityType != null ? entityType.name() : null;
    }

    @Named("stringToEntityType")
    default EntityType stringToEntityType(String entityType) {
        if (entityType == null) {
            return null;
        }
        try {
            return EntityType.valueOf(entityType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }

    @Named("domainListToJson")
    default String domainListToJson(List<DomainType> domainTypes) {
        if (domainTypes == null) {
            return null;
        }
        try {
            List<String> values = domainTypes.stream()
                .map(DomainType::name)
                .collect(Collectors.toList());
            return OBJECT_MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize target domains", e);
        }
    }

    @Named("jsonToDomainList")
    default List<DomainType> jsonToDomainList(String json) {
        if (json == null) {
            return null;
        }
        try {
            String[] domains = OBJECT_MAPPER.readValue(json, String[].class);
            return List.of(domains).stream()
                .map(this::stringToDomainType)
                .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize target domains", e);
        }
    }

}

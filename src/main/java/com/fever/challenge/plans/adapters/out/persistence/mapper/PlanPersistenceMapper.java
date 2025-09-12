package com.fever.challenge.plans.adapters.out.persistence.mapper;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.domain.model.Plan;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { TimeMapper.class })
public interface PlanPersistenceMapper {

    // Domain -> Entity (crear una entidad nueva)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "providerId", source = "id")
    @Mapping(target = "startsAt", expression = "java(TimeMapper.toInstant(plan.getStartDate(), plan.getStartTime()))")
    @Mapping(target = "endsAt",   expression = "java(TimeMapper.toInstant(plan.getEndDate(),   plan.getEndTime()))")
    @Mapping(target = "sellMode", ignore = true)
    @Mapping(target = "firstSeenAt", ignore = true)
    @Mapping(target = "lastSeenAt",  ignore = true)
    @Mapping(target = "currentlyAvailable", ignore = true)
    PlanEntity toEntity(Plan plan);

    // Actualizar una entidad existente con datos del dominio (sin tocar campos de infraestructura)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "startsAt", expression = "java(TimeMapper.toInstant(plan.getStartDate(), plan.getStartTime()))")
    @Mapping(target = "endsAt",   expression = "java(TimeMapper.toInstant(plan.getEndDate(),   plan.getEndTime()))")
    @Mapping(target = "sellMode", ignore = true)
    @Mapping(target = "firstSeenAt", ignore = true)
    @Mapping(target = "lastSeenAt",  ignore = true)
    @Mapping(target = "currentlyAvailable", ignore = true)
    void updateEntityFromDomain(Plan plan, @MappingTarget PlanEntity entity);

    // Entity -> Domain
    @Mapping(target = "id", source = "providerId")
    @Mapping(target = "startDate", expression = "java(TimeMapper.toDate(entity.getStartsAt()))")
    @Mapping(target = "startTime", expression = "java(TimeMapper.toTime(entity.getStartsAt()))")
    @Mapping(target = "endDate",   expression = "java(TimeMapper.toDate(entity.getEndsAt()))")
    @Mapping(target = "endTime",   expression = "java(TimeMapper.toTime(entity.getEndsAt()))")
    Plan toDomain(PlanEntity entity);
}

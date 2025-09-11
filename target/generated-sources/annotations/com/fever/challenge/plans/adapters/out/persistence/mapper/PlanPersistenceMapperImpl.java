package com.fever.challenge.plans.adapters.out.persistence.mapper;

import com.fever.challenge.plans.adapters.out.persistence.entity.PlanEntity;
import com.fever.challenge.plans.domain.model.Plan;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-11T12:56:13+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class PlanPersistenceMapperImpl implements PlanPersistenceMapper {

    @Override
    public PlanEntity toEntity(Plan plan) {
        if ( plan == null ) {
            return null;
        }

        PlanEntity.PlanEntityBuilder planEntity = PlanEntity.builder();

        planEntity.providerId( plan.getId() );
        planEntity.title( plan.getTitle() );
        planEntity.minPrice( plan.getMinPrice() );
        planEntity.maxPrice( plan.getMaxPrice() );

        planEntity.startsAt( TimeMapper.toInstant(plan.getStartDate(), plan.getStartTime()) );
        planEntity.endsAt( TimeMapper.toInstant(plan.getEndDate(),   plan.getEndTime()) );

        return planEntity.build();
    }

    @Override
    public Plan toDomain(PlanEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Plan.PlanBuilder plan = Plan.builder();

        plan.id( entity.getProviderId() );
        plan.title( entity.getTitle() );
        plan.minPrice( entity.getMinPrice() );
        plan.maxPrice( entity.getMaxPrice() );

        plan.startDate( TimeMapper.toDate(entity.getStartsAt()) );
        plan.startTime( TimeMapper.toTime(entity.getStartsAt()) );
        plan.endDate( TimeMapper.toDate(entity.getEndsAt()) );
        plan.endTime( TimeMapper.toTime(entity.getEndsAt()) );

        return plan.build();
    }
}

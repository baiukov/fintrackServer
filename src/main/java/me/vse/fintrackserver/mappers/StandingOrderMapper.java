package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.model.StandingOrder;
import me.vse.fintrackserver.rest.requests.StandingOrderRequest;
import me.vse.fintrackserver.rest.requests.TransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StandingOrderMapper {
    void updateStandingOrderFromRequest(StandingOrderRequest request, @MappingTarget StandingOrder entity);
}

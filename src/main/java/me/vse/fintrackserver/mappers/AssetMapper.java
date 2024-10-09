package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.dto.AssetDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssetMapper {
    void updateAccountFromDto(AssetDto dto, @MappingTarget Asset entity);
}

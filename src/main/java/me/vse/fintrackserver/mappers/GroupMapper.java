package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Group;
import me.vse.fintrackserver.model.dto.AccountDto;
import me.vse.fintrackserver.model.dto.GroupDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GroupMapper {
    void updateGroupFromDto(GroupDto dto, @MappingTarget Group entity);

}

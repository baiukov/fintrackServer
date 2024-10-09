package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.dto.AccountDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {
    void updateAccountFromDto(AccountDto dto, @MappingTarget Account entity);

}

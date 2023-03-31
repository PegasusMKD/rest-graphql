package com.rql.toy.example.mappers;

import com.rql.core.nodes.PropertyNode;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import com.rql.toy.example.dto.AccountDto;
import com.rql.toy.example.models.Account;

import java.util.Collection;
import java.util.List;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RealAccountMapper {

	RealAccountMapper INSTANCE = Mappers.getMapper(RealAccountMapper.class);
	AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

	@IterableMapping(qualifiedByName = "accountToDto")
	Collection<AccountDto> toAccountDtos(Collection<Account> entities, @Context List<PropertyNode> propertyNodes);

	// @InheritConfiguration(name = "rqlToAccountDto")
	@Named("accountToDto")
	default AccountDto toAccountDto(Account entity, @Context List<PropertyNode> propertyNodes) {
		return accountMapper.toAccountDtoDefault(entity, propertyNodes);
	}
}

package com.rql.toy.example.mappers;

import com.rest.graphql.rql.core.nodes.PropertyNode;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.rql.toy.example.dto.AccountDto;
import com.rql.toy.example.models.Account;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class RealAccountMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@IterableMapping(qualifiedByName = "accountToDto")
	public abstract Collection<AccountDto> toAccountDtos(Collection<Account> entities, @Context List<PropertyNode> propertyNodes);

	// @InheritConfiguration(name = "rqlToAccountDto")
	@Named("accountToDto")
	public AccountDto toAccountDto(Account entity, @Context List<PropertyNode> propertyNodes) {
		return accountMapper.toAccountDtoDefault(entity, propertyNodes);
	}
}

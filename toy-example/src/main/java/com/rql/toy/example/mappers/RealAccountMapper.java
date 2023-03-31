package com.rql.toy.example.mappers;

import com.rest.graphql.rql.core.nodes.PropertyNode;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import com.rql.toy.example.dto.AccountDto;
import com.rql.toy.example.models.Account;

import java.util.Collection;
import java.util.List;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RealAccountMapper {

	AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

	@IterableMapping(qualifiedByName = "accountToDto")
	Collection<AccountDto> toAccountDtos(Collection<Account> entities, @Context List<PropertyNode> propertyNodes);

	// @InheritConfiguration(name = "rqlToAccountDto")
	@Named("accountToDto")
	default AccountDto toAccountDto(Account entity, @Context List<PropertyNode> propertyNodes) {
		return accountMapper.toAccountDtoDefault(entity, propertyNodes);
	}
}

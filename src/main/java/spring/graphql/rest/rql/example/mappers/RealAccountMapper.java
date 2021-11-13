package spring.graphql.rest.rql.example.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.example.dto.AccountDto;
import spring.graphql.rest.rql.example.models.Account;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class RealAccountMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@IterableMapping(qualifiedByName = "accountToDto")
	public abstract Collection<AccountDto> toAccountDtos(Collection<Account> entities,
														 @Context List<PropertyNode> propertyNodes);

	@Named("accountToDto")
	public AccountDto toAccountDto(Account entity, @Context List<PropertyNode> propertyNodes) {
		entity.setUsername(null);
		return accountMapper.toAccountDtoDefault(entity, propertyNodes);
	}
}

package spring.graphql.rest.nonoptimized.example.mappers;


import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.UniversalMapper;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.models.Account;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {UniversalMapper.class})
public abstract class AccountMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@Autowired
	protected PostMapper postMapper;

	@Autowired
	protected PersonMapper personMapper;


	@IterableMapping(qualifiedByName = "dynamicAccounts")
	public abstract Set<AccountDto> toAccountDtos(Set<Account> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicAccounts")
	@Mapping(target = "friends", expression = "java(properties.contains(\"friends\") ? accountMapper.toAccountDtos(entity.getFriends(), currentPath, propertyNodes, properties, \"friends\") : null)")
	@Mapping(target = "posts", expression = "java(properties.contains(\"posts\") ? postMapper.toPostDtos(entity.getPosts(), currentPath, propertyNodes, properties, \"posts\") : null)")
	@Mapping(target = "person", expression = "java(properties.contains(\"person\") ? personMapper.toPersonDto(entity.getPerson(), currentPath, propertyNodes, properties, \"person\") : null)")
	public abstract AccountDto toAccountDto(Account entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);


}

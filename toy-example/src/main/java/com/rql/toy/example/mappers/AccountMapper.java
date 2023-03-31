package com.rql.toy.example.mappers;


import com.rest.graphql.rql.core.nodes.PropertyNode;
import com.rest.graphql.rql.core.nodes.TraversalMapper;
import com.rql.toy.example.dto.AccountDto;
import com.rql.toy.example.models.Account;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {TraversalMapper.class})
public abstract class AccountMapper {

	@Autowired
	@Qualifier("accountMapperImpl")
	protected AccountMapper accountMapper;

	@Autowired
	protected PostMapper postMapper;

	@Autowired
	protected PersonMapper personMapper;

	@Autowired
	protected CommentMapper commentMapper;

	@IterableMapping(qualifiedByName = "dynamicAccounts")
	public abstract Set<AccountDto> rqlToAccountDtos(Set<Account> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	// TODO: Implement automatic generation/addition of these mappers
	@Named("dynamicAccounts")
	@Mapping(target = "friends", expression = "java(properties.contains(\"friends\") ? accountMapper.rqlToAccountDtos(entity.getFriends(), currentPath, propertyNodes, properties, \"friends\") : null)")
	@Mapping(target = "posts", expression = "java(properties.contains(\"posts\") ? postMapper.toPostDtos(entity.getPosts(), currentPath, propertyNodes, properties, \"posts\") : null)")
	@Mapping(target = "comments", expression = "java(properties.contains(\"comments\") ? commentMapper.toCommentDtos(entity.getComments(), currentPath, propertyNodes, properties, \"comments\") : null)")
	@Mapping(target = "person", expression = "java(properties.contains(\"person\") ? personMapper.toPersonDto(entity.getPerson(), currentPath, propertyNodes, properties, \"person\") : null)")
	public abstract AccountDto rqlToAccountDto(Account entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicAccountsDefaultCollection")
	public Set<AccountDto> toAccountDtosDefault(Collection<Account> entity, @Context List<PropertyNode> propertyNodes) {
		return rqlToAccountDtos(new HashSet<>(entity), new StringBuilder(), propertyNodes, new ArrayList<>(), "");
	}

	@Named("dynamicAccountsDefault")
	public AccountDto toAccountDtoDefault(Account entity, @Context List<PropertyNode> propertyNodes) {
		return rqlToAccountDto(entity, new StringBuilder(), propertyNodes, new ArrayList<>(), "");
	}
}

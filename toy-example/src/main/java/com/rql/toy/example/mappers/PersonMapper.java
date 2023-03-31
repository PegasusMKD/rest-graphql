package com.rql.toy.example.mappers;

import com.rest.graphql.rql.core.nodes.PropertyNode;
import com.rest.graphql.rql.core.nodes.TraversalMapper;
import com.rql.toy.example.dto.PersonDto;
import com.rql.toy.example.models.Person;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {TraversalMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class PersonMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@IterableMapping(qualifiedByName = "dynamicPersons")
	public abstract Set<PersonDto> toPersonDtos(Set<Person> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicPersons")
	@Mapping(target = "account", expression = "java(properties.contains(\"account\") ? accountMapper.rqlToAccountDto(entity.getAccount(), currentPath, propertyNodes, properties, \"account\") : null)")
	public abstract PersonDto toPersonDto(Person entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

}

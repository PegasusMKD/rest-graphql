package spring.graphql.rest.nonoptimized.example.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.UniversalMapper;
import spring.graphql.rest.nonoptimized.example.dto.PersonDto;
import spring.graphql.rest.nonoptimized.example.models.Person;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		uses = {UniversalMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class PersonMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@IterableMapping(qualifiedByName = "dynamicPersons")
	public abstract Set<PersonDto> toPersonDtos(Set<Person> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicPersons")
	@Mapping(target = "account", expression = "java(properties.contains(\"account\") ? accountMapper.toAccountDto(entity.getAccount(), currentPath, propertyNodes, properties, \"account\") : null)")
	public abstract PersonDto toPersonDto(Person entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

}

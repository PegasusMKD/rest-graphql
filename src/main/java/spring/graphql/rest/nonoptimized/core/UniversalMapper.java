package spring.graphql.rest.nonoptimized.core;

import org.mapstruct.*;
import spring.graphql.rest.nonoptimized.example.dto.CommentDto;
import spring.graphql.rest.nonoptimized.example.dto.PersonDto;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.models.Account;
import spring.graphql.rest.nonoptimized.example.models.Comment;
import spring.graphql.rest.nonoptimized.example.models.Person;
import spring.graphql.rest.nonoptimized.example.models.Post;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class UniversalMapper {

	@IterableMapping(qualifiedByName = "dynamicAccounts")
	public abstract Set<AccountDto> toAccountDtos(Set<Account> entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);
	
	@Named("dynamicAccounts")
	@Mapping(target = "friends", expression = "java(properties.contains(\"friends\") ? toAccountDtos(entity.getFriends(), currentPath, propertyWrappers, properties, \"friends\") : null)")
	@Mapping(target = "posts", expression = "java(properties.contains(\"posts\") ? toPostDtos(entity.getPosts(), currentPath, propertyWrappers, properties, \"posts\") : null)")
	@Mapping(target = "person", expression = "java(properties.contains(\"person\") ? toPersonDto(entity.getPerson(), currentPath, propertyWrappers, properties, \"person\") : null)")
	public abstract AccountDto toAccountDto(Account entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);


	@IterableMapping(qualifiedByName = "dynamicPersons")
	public abstract Set<PersonDto> toPersonDtos(Set<Person> entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);

	@Named("dynamicPersons")
	@Mapping(target = "account", expression = "java(properties.contains(\"account\") ? toAccountDto(entity.getAccount(), currentPath, propertyWrappers, properties, \"account\") : null)")
	public abstract PersonDto toPersonDto(Person entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);


	@IterableMapping(qualifiedByName = "dynamicPosts")
	public abstract Set<PostDto> toPostDtos(Set<Post> entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);

	@Named("dynamicPosts")
	@Mapping(target = "postedBy", expression = "java(properties.contains(\"postedBy\") ? toAccountDto(entity.getPostedBy(), currentPath, propertyWrappers, properties, \"postedBy\") : null)")
	@Mapping(target = "comments", expression = "java(properties.contains(\"comments\") ? toCommentDtos(entity.getComments(), currentPath, propertyWrappers, properties, \"comments\") : null)")
	public abstract PostDto toPostDto(Post entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);


	@IterableMapping(qualifiedByName = "dynamicComments")
	public abstract Set<CommentDto> toCommentDtos(Set<Comment> entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);

	@Named("dynamicComments")
	@Mapping(target = "account", expression = "java(properties.contains(\"account\") ? toAccountDto(entity.getAccount(), currentPath, propertyWrappers, properties, \"account\") : null)")
	@Mapping(target = "post", expression = "java(properties.contains(\"post\") ? toPostDto(entity.getPost(), currentPath, propertyWrappers, properties, \"post\") : null)")
	public abstract CommentDto toCommentDto(Comment entity, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties, @Context String property);


	@BeforeMapping
	public void resetGenericPropertyWrappers(Object vals, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers,
											 @Context List<String> properties, @Context String property) {
		if(vals instanceof Collection || vals instanceof Map) {
			return;
		}
		if(currentPath.length() == 0) {
			currentPath.append(property);
		} else {
			currentPath.append(".").append(property);
		}
		updateProperties(currentPath, propertyWrappers, properties);
	}

	@AfterMapping
	public void cleanPath(Object vals, @Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers,
						  @Context List<String> properties, @Context String property) {
		if(vals instanceof Collection || vals instanceof Map) {
			return;
		}
		if(currentPath.toString().contains(".")) {
			currentPath.delete(currentPath.lastIndexOf("."), currentPath.length());
		} else {
			currentPath.delete(0, currentPath.length());
		}
		updateProperties(currentPath, propertyWrappers, properties);
	}

	private void updateProperties(@Context StringBuilder currentPath, @Context List<GenericPropertyWrapper> propertyWrappers, @Context List<String> properties) {
		properties.removeIf(val -> !val.isEmpty());
		properties.addAll(propertyWrappers.stream().filter(prop -> prop.getParentPropertyPath().equals(currentPath.toString()))
				.map(GenericPropertyWrapper::getProperty).collect(Collectors.toList()));
	}
}

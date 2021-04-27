package spring.graphql.rest.nonoptimized.example.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.UniversalMapper;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
import spring.graphql.rest.nonoptimized.example.models.Post;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {UniversalMapper.class})
public abstract class PostMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@Autowired
	protected CommentMapper commentMapper;

	@IterableMapping(qualifiedByName = "dynamicPosts")
	public abstract Set<PostDto> toPostDtos(Set<Post> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicPosts")
	@Mapping(target = "postedBy", expression = "java(properties.contains(\"postedBy\") ? accountMapper.toAccountDto(entity.getPostedBy(), currentPath, propertyNodes, properties, \"postedBy\") : null)")
	@Mapping(target = "comments", expression = "java(properties.contains(\"comments\") ? commentMapper.toCommentDtos(entity.getComments(), currentPath, propertyNodes, properties, \"comments\") : null)")
	public abstract PostDto toPostDto(Post entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

}

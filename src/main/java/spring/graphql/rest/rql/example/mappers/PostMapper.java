package spring.graphql.rest.rql.example.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.core.nodes.TraversalMapper;
import spring.graphql.rest.rql.example.dto.PostDto;
import spring.graphql.rest.rql.example.models.Post;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {TraversalMapper.class})
public abstract class PostMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@Autowired
	protected CommentMapper commentMapper;

	@IterableMapping(qualifiedByName = "dynamicPosts")
	public abstract Set<PostDto> toPostDtos(Set<Post> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicPosts")
	@Mapping(target = "postedBy", expression = "java(properties.contains(\"postedBy\") ? accountMapper.rqlToAccountDto(entity.getPostedBy(), currentPath, propertyNodes, properties, \"postedBy\") : null)")
	@Mapping(target = "comments", expression = "java(properties.contains(\"comments\") ? commentMapper.toCommentDtos(entity.getComments(), currentPath, propertyNodes, properties, \"comments\") : null)")
	public abstract PostDto toPostDto(Post entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

}

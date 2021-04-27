package spring.graphql.rest.nonoptimized.example.mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.UniversalMapper;
import spring.graphql.rest.nonoptimized.example.dto.CommentDto;
import spring.graphql.rest.nonoptimized.example.models.Comment;

import java.util.List;
import java.util.Set;

@Repository
@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {UniversalMapper.class})
public abstract class CommentMapper {

	@Autowired
	protected AccountMapper accountMapper;

	@Autowired
	protected PostMapper postMapper;

	@IterableMapping(qualifiedByName = "dynamicComments")
	public abstract Set<CommentDto> toCommentDtos(Set<Comment> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicComments")
	@Mapping(target = "account", expression = "java(properties.contains(\"account\") ? accountMapper.toAccountDto(entity.getAccount(), currentPath, propertyNodes, properties, \"account\") : null)")
	@Mapping(target = "post", expression = "java(properties.contains(\"post\") ? postMapper.toPostDto(entity.getPost(), currentPath, propertyNodes, properties, \"post\") : null)")
	public abstract CommentDto toCommentDto(Comment entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

}

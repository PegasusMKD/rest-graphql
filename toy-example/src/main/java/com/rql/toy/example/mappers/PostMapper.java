package com.rql.toy.example.mappers;

import com.rql.core.nodes.PropertyNode;
import com.rql.core.nodes.TraversalMapper;
import com.rql.toy.example.models.Post;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import com.rql.toy.example.dto.PostDto;

import java.util.List;
import java.util.Set;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {TraversalMapper.class})
public interface PostMapper {
	PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

	AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

	CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

	@IterableMapping(qualifiedByName = "dynamicPosts")
	Set<PostDto> toPostDtos(Set<Post> entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

	@Named("dynamicPosts")
	@Mapping(target = "postedBy", expression = "java(properties.contains(\"postedBy\") ? accountMapper.rqlToAccountDto(entity.getPostedBy(), currentPath, propertyNodes, properties, \"postedBy\") : null)")
	@Mapping(target = "comments", expression = "java(properties.contains(\"comments\") ? commentMapper.toCommentDtos(entity.getComments(), currentPath, propertyNodes, properties, \"comments\") : null)")
	PostDto toPostDto(Post entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties, @Context String property);

}

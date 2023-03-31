package com.rql.toy.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.rql.core.RQL;
import com.rql.core.nodes.PropertyNode;
import com.rql.core.utility.GraphUtility;
import com.rql.toy.example.models.QPost;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rql.toy.example.controller.rest.PageRequestByExample;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.dto.PostDto;
import com.rql.toy.example.dto.querydsl.OptionalBooleanBuilder;
import com.rql.toy.example.mappers.PostMapper;
import com.rql.toy.example.models.Post;
import com.rql.toy.example.repository.PostRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PostService {

	private Logger logger = LoggerFactory.getLogger(PostService.class);

	private final PostRepository postRepository;

	private final AccountService accountService;

	private final GraphUtility graphUtility;
	private final RQL rql;

	private BooleanExpression makeFilter(PostDto dto) {
		return makeFilter(dto, Optional.empty(), Optional.empty()).build();
	}

	public OptionalBooleanBuilder makeFilter(PostDto dto, Optional<QPost> _qPost, Optional<OptionalBooleanBuilder> _opBuilder) {
		QPost qPost = _qPost.orElse(QPost.post);
		OptionalBooleanBuilder opBuilder = _opBuilder.orElse(OptionalBooleanBuilder.builder(qPost.isNotNull()));
		if (dto == null) {
			return opBuilder;
		}

		opBuilder = accountService.makeFilter(dto.getPostedBy(), Optional.of(qPost.postedBy), Optional.of(opBuilder));

		return opBuilder.notNullAnd(qPost.id::eq, dto.getId())
				.notEmptyAnd(qPost.content::containsIgnoreCase, dto.getContent());
	}

	@Transactional(readOnly = true)
	public PageResponse<PostDto> findAllPosts(PageRequestByExample<PostDto> prbe, String[] attributePaths) {
		PostDto example = prbe.getExample() != null ? prbe.getExample() : new PostDto();

		Page<Post> page = rql.rqlSelect(
				(EntityGraph graph) -> postRepository.findAll(makeFilter(example), prbe.toPageable(), graph),
				Slice::getContent, Post.class, attributePaths);

		// TODO: Implement mapping as "special" feature/option
//		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = graphUtility.createPropertyNodes(Post.class, attributePaths);
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<PostDto> content = new ArrayList<>(PostMapper.INSTANCE.toPostDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyNodes, props, ""));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

}

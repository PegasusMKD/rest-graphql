package com.rql.toy.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain2.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraphType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.rql.core.nodes.PropertyNode;
import com.rql.core.utility.GraphUtility;
import com.rql.toy.example.controller.rest.PageRequestByExample;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.dto.CommentDto;
import com.rql.toy.example.models.QComment;
import com.rql.toy.example.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.rql.toy.example.dto.querydsl.OptionalBooleanBuilder;
import com.rql.toy.example.mappers.CommentMapper;
import com.rql.toy.example.models.Comment;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentService {

	private Logger logger = LoggerFactory.getLogger(CommentService.class);

	private final CommentRepository commentRepository;

	private final PostService postService;

	private final AccountService accountService;

	private final GraphUtility graphUtility;

	private BooleanExpression makeFilter(CommentDto dto) {
		return makeFilter(dto, Optional.empty(), Optional.empty()).build();
	}

	public OptionalBooleanBuilder makeFilter(CommentDto dto, Optional<QComment> _qComment, Optional<OptionalBooleanBuilder> _opBuilder) {
		QComment qComment = _qComment.orElse(QComment.comment);
		OptionalBooleanBuilder opBuilder = _opBuilder.orElse(OptionalBooleanBuilder.builder(qComment.isNotNull()));
		if (dto == null) {
			return opBuilder;
		}

		opBuilder = postService.makeFilter(dto.getPost(), Optional.of(qComment.post), Optional.of(opBuilder));
		opBuilder = accountService.makeFilter(dto.getAccount(), Optional.of(qComment.account), Optional.of(opBuilder));

		return opBuilder.notNullAnd(qComment.id::eq, dto.getId())
				.notEmptyAnd(qComment.content::containsIgnoreCase, dto.getContent());
	}

	@Transactional
	public PageResponse<CommentDto> findAllComments(PageRequestByExample<CommentDto> prbe, String[] attributePaths) {
		CommentDto example = prbe.getExample() != null ? prbe.getExample() : new CommentDto();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = graphUtility.createPropertyNodes(Comment.class, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).toList();
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Comments", (endTime - startTime) / 1000000);

		// Fetch data
		DynamicEntityGraph.Builder graph = DynamicEntityGraph.builder(EntityGraphType.LOAD);
		paths.forEach(graph::addPath);
		Page<Comment> page = commentRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				DynamicEntityGraph.NOOP : graph.build());

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<CommentDto> content = new ArrayList<>(CommentMapper.INSTANCE.toCommentDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyNodes, props, ""));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Comments", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

}

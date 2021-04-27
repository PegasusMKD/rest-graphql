package spring.graphql.rest.nonoptimized.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.querydsl.OptionalBooleanBuilder;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.core.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.CommentDto;
import spring.graphql.rest.nonoptimized.example.mappers.CommentMapper;
import spring.graphql.rest.nonoptimized.example.models.Comment;
import spring.graphql.rest.nonoptimized.example.models.QComment;
import spring.graphql.rest.nonoptimized.example.repository.CommentRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.Helpers.getGenericPropertyWrappers;

@Service
public class CommentService {

	private Logger logger = LoggerFactory.getLogger(CommentService.class);

	private final CommentRepository commentRepository;

	private final CommentMapper commentMapper;

	private final PostService postService;

	private final AccountService accountService;

	public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, PostService postService, AccountService accountService) {
		this.commentRepository = commentRepository;
		this.commentMapper = commentMapper;
		this.postService = postService;
		this.accountService = accountService;
	}

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
		CommentDto example = prbe.getExample();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = getGenericPropertyWrappers(Comment.class, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Comments", (endTime - startTime) / 1000000);

		// Fetch data
		Page<Comment> page = commentRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<CommentDto> content = new ArrayList<>(commentMapper.toCommentDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyNodes, props, ""));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Comments", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

}

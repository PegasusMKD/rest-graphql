package spring.graphql.rest.nonoptimized.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.RQL;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.example.controller.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.example.controller.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
import spring.graphql.rest.nonoptimized.example.dto.querydsl.OptionalBooleanBuilder;
import spring.graphql.rest.nonoptimized.example.mappers.PostMapper;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.models.QPost;
import spring.graphql.rest.nonoptimized.example.repository.PostRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static spring.graphql.rest.nonoptimized.core.utility.GraphUtility.createPropertyNodes;

@Service
public class PostService {

	private Logger logger = LoggerFactory.getLogger(PostService.class);

	private final PostRepository postRepository;

	private final PostMapper postMapper;

	private final AccountService accountService;

	private final RQL rql;

	public PostService(PostRepository postRepository, PostMapper postMapper, AccountService accountService, RQL rql) {
		this.postRepository = postRepository;
		this.postMapper = postMapper;
		this.accountService = accountService;
		this.rql = rql;
	}

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

	public PageResponse<PostDto> findAllPosts(PageRequestByExample<PostDto> prbe, String[] attributePaths) {
		PostDto example = prbe.getExample() != null ? prbe.getExample() : new PostDto();

		Page<Post> page = rql.efficientCollectionFetch(
				(EntityGraph graph) -> postRepository.findAll(makeFilter(example), prbe.toPageable(), graph),
				Slice::getContent, Post.class, attributePaths);

		// TODO: Implement mapping as "special" feature/option
//		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = createPropertyNodes(Post.class, attributePaths);
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<PostDto> content = new ArrayList<>(postMapper.toPostDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyNodes, props, ""));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

}

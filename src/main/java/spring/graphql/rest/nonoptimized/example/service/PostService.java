package spring.graphql.rest.nonoptimized.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.querydsl.OptionalBooleanBuilder;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.core.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
import spring.graphql.rest.nonoptimized.example.mappers.PostMapper;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.models.QPost;
import spring.graphql.rest.nonoptimized.example.processors.RQLMainProcessingUnit;
import spring.graphql.rest.nonoptimized.example.repository.PostRepository;
import spring.graphql.rest.nonoptimized.experimental.LambdaAndMethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.getGenericPropertyWrappers;

@Service
public class PostService {

	private Logger logger = LoggerFactory.getLogger(PostService.class);

	private final PostRepository postRepository;

	private final PostMapper postMapper;

	private final AccountService accountService;

	private final RQLMainProcessingUnit rqlMainProcessingUnit;

	private final LambdaAndMethods lambdaAndMethods;

	public PostService(PostRepository postRepository, PostMapper postMapper, AccountService accountService, RQLMainProcessingUnit rqlMainProcessingUnit, LambdaAndMethods lambdaAndMethods) {
		this.postRepository = postRepository;
		this.postMapper = postMapper;
		this.accountService = accountService;
		this.rqlMainProcessingUnit = rqlMainProcessingUnit;
		this.lambdaAndMethods = lambdaAndMethods;
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

	public PageResponse<PostDto> findAllPosts(PageRequestByExample<PostDto> prbe, String[] attributePaths) throws NoSuchMethodException, IllegalAccessException {
		PostDto example = prbe.getExample();

//		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = getGenericPropertyWrappers(Post.class, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);

//		boolean containsRelation = false;
//
//		if(paths.contains("comments")) {
//			paths.removeIf(val -> val.contains("comments"));
//			containsRelation = true;
//		}
//
//		// Fetch data
//		Page<Post> page = postRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
//				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));
//
//		startTime = System.nanoTime();
//		if(containsRelation) {
//			rqlMainProcessingUnit.process(page.getContent(), propertyNodes.stream().filter(val -> val.getProperty().equals("comments")).findAny().get(), propertyNodes);
//		}
//		endTime = System.nanoTime();
//		logger.info("Fetch posts: {} ms -- Posts", (endTime - startTime) / 1000000);

		Page<Post> page = lambdaAndMethods.efficientCollectionFetch((EntityGraph graph) -> postRepository.findAll(makeFilter(example), prbe.toPageable(), graph),
				Slice::getContent, Post.class, attributePaths);

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

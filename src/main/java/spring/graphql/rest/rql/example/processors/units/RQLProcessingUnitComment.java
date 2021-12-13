package spring.graphql.rest.rql.example.processors.units;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.rql.core.RQL;
import spring.graphql.rest.rql.core.dto.TransferResultDto;
import spring.graphql.rest.rql.core.internal.RQLInternal;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.core.processing.RQLProcessingUnit;
import spring.graphql.rest.rql.core.utility.EntityGraphUtility;
import spring.graphql.rest.rql.core.utility.GraphUtility;
import spring.graphql.rest.rql.example.models.Comment;
import spring.graphql.rest.rql.example.processors.repository.RQLCommentRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.rql.core.utility.GraphUtility.*;

@Service
@Qualifier("RQLComment")
public class RQLProcessingUnitComment implements RQLProcessingUnit<Comment> {

	private final RQLCommentRepository rqlCommentRepository;

	private final RQLInternal rqlInternal;

	private final RQL rql;

	@Lazy
	public RQLProcessingUnitComment(RQLCommentRepository rqlCommentRepository, RQLInternal rqlInternal, RQL rql) {
		this.rqlCommentRepository = rqlCommentRepository;
		this.rqlInternal = rqlInternal;
		this.rql = rql;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Comment> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty) {
		List<PropertyNode> subPartition = getSubPartition(tree, node);
		List<PropertyNode> currentPartition = getCurrentValidPartition(subPartition, node.getGraphPath())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = GraphUtility.getProcessedPaths(currentPartition, node);
		List<Comment> result = callProperQuery(parentAccessProperty, ids, paths);
		subPartition.forEach(_node -> completeNode(node, currentPartition, _node));


		rqlInternal.processSubPartitions(subPartition, result, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, result);
	}

	private List<Comment> callProperQuery(String parentAccessProperty, Set<String> ids, List<String> paths) {
		switch (parentAccessProperty) {
			case "account":
				return rqlCommentRepository.findAllByAccountIdIn(ids, EntityGraphUtility.getEagerEntityGraph(paths));
//						rql.asyncRQLSelectPagination(RQLAsyncRestriction.THREAD_COUNT, 5,
//						(EntityGraph graph, Pageable pageable) -> rqlCommentRepository.findAllByAccountIdIn(ids, pageable, graph),
//						wrapper -> wrapper, LazyLoadEvent.builder().first(0)
//								.rows(rqlCommentRepository.countAllByAccountIdIn(ids))
//								.build(), Comment.class, paths.toArray(new String[0]));
			case "post":
				return rqlCommentRepository.findAllByPostIdIn(ids, EntityGraphUtility.getEagerEntityGraph(paths));
//				rql.asyncRQLSelectPagination(RQLAsyncRestriction.THREAD_COUNT, 5,
//						(EntityGraph graph, Pageable pageable) -> rqlCommentRepository.findAllByPostIdIn(ids, pageable, graph),
//						wrapper -> wrapper, LazyLoadEvent.builder().first(0)
//								.rows(rqlCommentRepository.countAllByPostIdIn(ids))
//								.build(), Comment.class, paths.toArray(new String[0]));
		}

		throw new RuntimeException();
	}

}

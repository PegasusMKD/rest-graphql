package com.rql.toy.example.processors.units;

import com.rest.graphql.rql.core.RQL;
import com.rest.graphql.rql.core.dto.TransferResultDto;
import com.rest.graphql.rql.core.internal.RQLInternal;
import com.rest.graphql.rql.core.nodes.PropertyNode;
import com.rest.graphql.rql.core.processing.RQLProcessingUnit;
import com.rest.graphql.rql.core.utility.EntityGraphUtility;
import com.rest.graphql.rql.core.utility.GraphUtility;
import com.rql.toy.example.models.Post;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rql.toy.example.processors.repository.RQLPostRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rest.graphql.rql.core.utility.GraphUtility.*;

// TODO: Implement separation by property
@Service
@Qualifier("RQLPost")
public class RQLProcessingUnitPost implements RQLProcessingUnit<Post> {

	private final RQLPostRepository rqlPostRepository;

	private final RQLInternal rqlInternal;

	private final RQL rql;

	public RQLProcessingUnitPost(RQLPostRepository rqlPostRepository, RQLInternal rqlInternal, RQL rql) {
		this.rqlPostRepository = rqlPostRepository;
		this.rqlInternal = rqlInternal;
		this.rql = rql;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Post> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty) {
		List<PropertyNode> subPartition = getSubPartition(tree, node);
		List<PropertyNode> currentPartition = getCurrentValidPartition(subPartition, node.getGraphPath())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = GraphUtility.getProcessedPaths(currentPartition, node);

//		List<Post> result = rql.asyncRQLSelectPagination(RQLAsyncRestriction.THREAD_COUNT, 5,
//				(EntityGraph graph, Pageable pageable) -> rqlPostRepository.findAllByPostedByIdIn(ids, pageable, graph),
//				wrapper -> wrapper, LazyLoadEvent.builder().first(0)
//						.rows(rqlPostRepository.countAllByPostedByIdIn(ids))
//						.build(), Post.class, paths.toArray(new String[0]));
		List<Post> result = rqlPostRepository.findAllByPostedByIdIn(ids, EntityGraphUtility.getEagerEntityGraph(paths));

		subPartition.forEach(_node -> completeNode(node, currentPartition, _node));

		rqlInternal.processSubPartitions(subPartition, result, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, result);
	}

}

package com.rql.toy.example.processors.units;

import com.rql.core.RQL;
import com.rql.core.dto.TransferResultDto;
import com.rql.core.internal.RQLInternal;
import com.rql.core.nodes.PropertyNode;
import com.rql.core.processing.RQLProcessingUnit;
import com.rql.core.utility.EntityGraphUtility;
import com.rql.core.utility.GraphUtility;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rql.toy.example.models.Comment;
import com.rql.toy.example.processors.repository.RQLCommentRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Qualifier("RQLComment")
public class RQLProcessingUnitComment implements RQLProcessingUnit<Comment> {

	private final RQLCommentRepository rqlCommentRepository;

	private final RQLInternal rqlInternal;

	private final RQL rql;

	private final GraphUtility graphUtility;
	private final EntityGraphUtility entityGraphUtility;

	@Lazy
	public RQLProcessingUnitComment(RQLCommentRepository rqlCommentRepository, RQLInternal rqlInternal, RQL rql, GraphUtility graphUtility, EntityGraphUtility entityGraphUtility) {
		this.rqlCommentRepository = rqlCommentRepository;
		this.rqlInternal = rqlInternal;
		this.rql = rql;
		this.graphUtility = graphUtility;
		this.entityGraphUtility = entityGraphUtility;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Comment> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty) {
		List<PropertyNode> subPartition = graphUtility.getSubPartition(tree, node);
		List<PropertyNode> currentPartition = graphUtility.getCurrentValidPartition(subPartition, node.getGraphPath())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = graphUtility.getProcessedPaths(currentPartition, node);
		List<Comment> result = callProperQuery(parentAccessProperty, ids, paths);
		subPartition.forEach(_node -> graphUtility.completeNode(node, currentPartition, _node));


		rqlInternal.processSubPartitions(subPartition, result, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, result);
	}

	@Override
	public List<Comment> callProperQuery(String parentAccessProperty, Set<String> ids, List<String> paths) {
		switch (parentAccessProperty) {
			case "account":
				return rqlCommentRepository.findAllByAccountIdIn(ids, entityGraphUtility.getEagerEntityGraph(paths));
			case "post":
				return rqlCommentRepository.findAllByPostIdIn(ids, entityGraphUtility.getEagerEntityGraph(paths));
		}

		throw new RuntimeException();
	}

}

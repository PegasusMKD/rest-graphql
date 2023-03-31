package com.rql.toy.example.processors.units;

import com.rql.core.RQL;
import com.rql.core.dto.TransferResultDto;
import com.rql.core.internal.RQLInternal;
import com.rql.core.nodes.PropertyNode;
import com.rql.core.processing.RQLProcessingUnit;
import com.rql.core.utility.EntityGraphUtility;
import com.rql.core.utility.GraphUtility;
import com.rql.toy.example.models.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rql.toy.example.processors.repository.RQLPostRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rql.core.utility.GraphUtility.*;

// TODO: Implement separation by property
@Service
@Qualifier("RQLPost")
@RequiredArgsConstructor
public class RQLProcessingUnitPost implements RQLProcessingUnit<Post> {

	private final RQLPostRepository rqlPostRepository;

	private final RQLInternal rqlInternal;

	private final RQL rql;

	private final GraphUtility graphUtility;
	private final EntityGraphUtility entityGraphUtility;

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Post> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty) {
		List<PropertyNode> subPartition = graphUtility.getSubPartition(tree, node);
		List<PropertyNode> currentPartition = graphUtility.getCurrentValidPartition(subPartition, node.getGraphPath())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = graphUtility.getProcessedPaths(currentPartition, node);

		List<Post> result = callProperQuery(parentAccessProperty, ids, paths);

		subPartition.forEach(_node -> graphUtility.completeNode(node, currentPartition, _node));

		rqlInternal.processSubPartitions(subPartition, result, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, result);
	}

	@Override
	public List<Post> callProperQuery(String parentAccessProperty, Set<String> ids, List<String> paths) {
		switch (parentAccessProperty) {
			case "postedBy":
				return rqlPostRepository.findAllByPostedByIdIn(ids, entityGraphUtility.getEagerEntityGraph(paths));
		}

		throw new RuntimeException();
	}

}

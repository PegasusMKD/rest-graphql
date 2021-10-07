package spring.graphql.rest.nonoptimized.example.processors.units;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.RQLInternal;
import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.core.utility.EntityGraphUtility;
import spring.graphql.rest.nonoptimized.core.utility.GraphUtility;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLPostRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.utility.GraphUtility.*;

// TODO: Implement separation by property
@Service
@Qualifier("RQLPost")
public class RQLProcessingUnitPost implements RQLProcessingUnit<Post> {

	private final RQLPostRepository rqlPostRepository;

	private final RQLInternal rqlInternal;

	public RQLProcessingUnitPost(RQLPostRepository rqlPostRepository, RQLInternal rqlInternal) {
		this.rqlPostRepository = rqlPostRepository;
		this.rqlInternal = rqlInternal;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Post> process(List<PropertyNode> partition, Set<String> ids, PropertyNode node, String parentAccessProperty) {
		List<PropertyNode> subPartition = getSubPartition(partition, node);

		List<PropertyNode> currentPartition = getCurrentValidPartition(subPartition, node.getProperty())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = GraphUtility.getProcessedPaths(currentPartition, node);
		List<Post> result = rqlPostRepository.findAllByPostedByIdIn(ids, EntityGraphUtility.getEagerEntityGraph(paths));
		subPartition.forEach(_node -> completeNode(node, currentPartition, _node));

		rqlInternal.processSubPartitions(subPartition, result, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, result);
	}

}

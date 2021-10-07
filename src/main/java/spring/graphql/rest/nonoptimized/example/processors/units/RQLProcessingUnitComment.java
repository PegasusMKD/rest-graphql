package spring.graphql.rest.nonoptimized.example.processors.units;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.RQLInternal;
import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.core.utility.EntityGraphUtility;
import spring.graphql.rest.nonoptimized.core.utility.GraphUtility;
import spring.graphql.rest.nonoptimized.example.models.Comment;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLCommentRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.utility.GraphUtility.completeNode;
import static spring.graphql.rest.nonoptimized.core.utility.GraphUtility.getSubTree;

@Service
@Qualifier("RQLComment")
public class RQLProcessingUnitComment implements RQLProcessingUnit<Comment> {

	private final RQLCommentRepository rqlCommentRepository;

	private final RQLInternal rqlInternal;

	@Lazy
	public RQLProcessingUnitComment(RQLCommentRepository rqlCommentRepository, RQLInternal rqlInternal) {
		this.rqlCommentRepository = rqlCommentRepository;
		this.rqlInternal = rqlInternal;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Comment> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty) {
		List<PropertyNode> subTree = getSubTree(tree, node);

		List<PropertyNode> currentTree = subTree.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = GraphUtility.getProcessedPaths(currentTree, node);
		List<Comment> res = callProperQuery(parentAccessProperty, ids, paths);
		subTree.forEach(el -> completeNode(node, currentTree, el));

		rqlInternal.processSubPartitions(subTree, res, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, res);
	}

	private List<Comment> callProperQuery(String propertyToParent, Set<String> data, List<String> paths) {
		switch (propertyToParent) {
			case "account":
				return rqlCommentRepository.findAllByAccountIdIn(data, EntityGraphUtility.getEagerEntityGraph(paths));
			case "post":
				return rqlCommentRepository.findAllByPostIdIn(data, EntityGraphUtility.getEagerEntityGraph(paths));
		}

		throw new RuntimeException();
	}

}

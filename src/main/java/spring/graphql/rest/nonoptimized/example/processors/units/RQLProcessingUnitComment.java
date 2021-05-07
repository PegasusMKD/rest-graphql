package spring.graphql.rest.nonoptimized.example.processors.units;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.example.models.Comment;
import spring.graphql.rest.nonoptimized.example.processors.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLCommentRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.completeNode;
import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.getSubTree;

@Service
@Qualifier("RQLComment")
public class RQLProcessingUnitComment implements RQLProcessingUnit<Comment> {

	private final RQLCommentRepository rqlCommentRepository;

	public RQLProcessingUnitComment(RQLCommentRepository rqlCommentRepository) {
		this.rqlCommentRepository = rqlCommentRepository;
	}

	@Override
	public TransferResultDto<Comment> process(List<PropertyNode> tree, Set<String> data, PropertyNode node, String propertyToParent) {
		List<PropertyNode> subTree = getSubTree(tree, node);

		List<PropertyNode> currentTree = subTree.stream().filter(val -> !val.isOneToMany()).collect(Collectors.toList());
		List<String> paths = GraphHelpers.getProcessedPaths(currentTree, node);

		List<Comment> res = rqlCommentRepository.findAllByPostIdIn(data, GraphHelpers.getEntityGraph(paths));
		subTree.forEach(el -> completeNode(node, currentTree, el));

		if(subTree.stream().anyMatch(val -> !val.isCompleted())) {
			// TODO: Implement generic function for fetch/processingUnit call
		}

		return new TransferResultDto<>(propertyToParent, res);
	}
}

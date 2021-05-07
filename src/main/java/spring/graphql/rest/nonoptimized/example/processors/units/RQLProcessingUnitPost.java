package spring.graphql.rest.nonoptimized.example.processors.units;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.processors.RQLMainProcessingUnit;
import spring.graphql.rest.nonoptimized.example.processors.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLPostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.completeNode;
import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.getSubTree;

// TODO: Implement separation by property
@Service
@Qualifier("RQLPost")
public class RQLProcessingUnitPost implements RQLProcessingUnit<Post> {

	private final RQLPostRepository rqlPostRepository;

	private final RQLMainProcessingUnit rqlMainProcessingUnit;

	public RQLProcessingUnitPost(RQLPostRepository rqlPostRepository, RQLMainProcessingUnit rqlMainProcessingUnit) {
		this.rqlPostRepository = rqlPostRepository;
		this.rqlMainProcessingUnit = rqlMainProcessingUnit;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Post> process(List<PropertyNode> tree, Set<String> data, PropertyNode node, String propertyToParent) {
		List<PropertyNode> subTree = getSubTree(tree, node);

		List<PropertyNode> temporaryCurrentTree = subTree.stream().filter(val -> !val.isOneToMany()).collect(Collectors.toList());

		List<PropertyNode> excludedTree = subTree.stream().filter(PropertyNode::isOneToMany).collect(Collectors.toList());
		List<String> excludedPaths = GraphHelpers.getPaths(excludedTree);

		List<PropertyNode> currentTree = new ArrayList<>();
		for(PropertyNode prop: temporaryCurrentTree) {
			boolean exclude = false;
			for(String path: excludedPaths) {
				if(prop.getParentPropertyPath().contains(path)) {
					exclude = true;
					break;
				}
			}

			if(!exclude) {
				currentTree.add(prop);
			}
		}

		List<String> paths = GraphHelpers.getProcessedPaths(currentTree, node);

		List<Post> res = rqlPostRepository.findAllByPostedByIdIn(data, GraphHelpers.getEntityGraph(paths));
		subTree.forEach(el -> completeNode(node, currentTree, el));

		if(subTree.stream().anyMatch(val -> !val.isCompleted())) {
			// TODO: Implement generic function for fetch/processingUnit call
			subTree = subTree.stream().filter(val -> !val.isCompleted()).collect(Collectors.toList());
			for(PropertyNode subNode: subTree) {
				try {
					if(subNode.isOneToMany()) {
						rqlMainProcessingUnit.process(res, subNode, subTree);
					}
				} catch (NoSuchMethodException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return new TransferResultDto<>(propertyToParent, res);
	}

}

package spring.graphql.rest.nonoptimized.example.processors.units;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.RQL;
import spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.processors.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLPostRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.*;

// TODO: Implement separation by property
@Service
@Qualifier("RQLPost")
public class RQLProcessingUnitPost implements RQLProcessingUnit<Post> {

	private final RQLPostRepository rqlPostRepository;

	private final RQL rql;

	public RQLProcessingUnitPost(RQLPostRepository rqlPostRepository, RQL rql) {
		this.rqlPostRepository = rqlPostRepository;
		this.rql = rql;
	}

	@Override
	@Transactional(readOnly = true)
	public TransferResultDto<Post> process(List<PropertyNode> tree, Set<String> data, PropertyNode node, String propertyToParent) {
		List<PropertyNode> subTree = getSubTree(tree, node);

//		List<PropertyNode> temporaryCurrentTree = subTree.stream().filter(val -> !val.isOneToMany()).collect(Collectors.toList());
//
//		List<PropertyNode> excludedTree = subTree.stream().filter(PropertyNode::isOneToMany).collect(Collectors.toList());
//		List<String> excludedPaths = GraphHelpers.getPaths(excludedTree);
//
//		List<PropertyNode> currentTree = new ArrayList<>();
//		for(PropertyNode prop: temporaryCurrentTree) {
//			boolean exclude = false;
//			for(String path: excludedPaths) {
//				if(prop.getParentPropertyPath().contains(path)) {
//					exclude = true;
//					break;
//				}
//			}
//
//			if(!exclude) {
//				currentTree.add(prop);
//			}
//		}

		List<PropertyNode> currentTree = getCurrentLevel(subTree, node.getProperty())
				.stream().filter(val -> !val.isOneToMany()).collect(Collectors.toList());
		List<String> paths = GraphHelpers.getProcessedPaths(currentTree, node);
		List<Post> res = rqlPostRepository.findAllByPostedByIdIn(data, GraphHelpers.getEntityGraph(paths));
		subTree.forEach(el -> completeNode(node, currentTree, el));

		rql.processSubTrees(subTree, res, node.getProperty());
		return new TransferResultDto<>(propertyToParent, res);
	}

}

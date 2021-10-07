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
	public TransferResultDto<Post> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty) {
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

		List<PropertyNode> currentTree = getCurrentPartition(subTree, node.getProperty())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = GraphUtility.getProcessedPaths(currentTree, node);
		List<Post> res = rqlPostRepository.findAllByPostedByIdIn(ids, EntityGraphUtility.getEagerEntityGraph(paths));
		subTree.forEach(el -> completeNode(node, currentTree, el));

		rqlInternal.processSubPartitions(subTree, res, node.getProperty());
		return new TransferResultDto<>(parentAccessProperty, res);
	}

}

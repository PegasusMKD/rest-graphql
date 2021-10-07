package spring.graphql.rest.nonoptimized.core;

import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLMainProcessingUnit;

import java.util.List;

import static spring.graphql.rest.nonoptimized.core.utility.GraphUtility.getCurrentPartition;

@Service
public class RQLInternal {

	private final RQLMainProcessingUnit rqlMainProcessingUnit;

	public RQLInternal(RQLMainProcessingUnit rqlMainProcessingUnit) {
		this.rqlMainProcessingUnit = rqlMainProcessingUnit;
	}

	public <K> void processSubPartitions(List<PropertyNode> propertyNodes, List<K> parents, String currentPath) {
		if (propertyNodes.stream().allMatch(PropertyNode::isCompleted)) {
			return;
		}

		getCurrentPartition(propertyNodes, currentPath)
				.stream().filter(node -> !node.isCompleted())
				.filter(PropertyNode::isOneToMany).forEach(node -> {
					try {
						rqlMainProcessingUnit.process(parents, node, propertyNodes);
					} catch (NoSuchMethodException | IllegalAccessException e) {
						// TODO: Implement proper error-handling
						e.printStackTrace();
					}
				});
	}
}

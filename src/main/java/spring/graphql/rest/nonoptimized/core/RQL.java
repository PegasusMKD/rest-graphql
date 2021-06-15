package spring.graphql.rest.nonoptimized.core;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.interfaces.InputFunction;
import spring.graphql.rest.nonoptimized.core.interfaces.ValueFetcher;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.example.processors.RQLMainProcessingUnit;

import java.util.List;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.*;

@Service
public class RQL {

	private final RQLMainProcessingUnit rqlMainProcessingUnit;

	public RQL(RQLMainProcessingUnit rqlMainProcessingUnit) {
		this.rqlMainProcessingUnit = rqlMainProcessingUnit;
	}

	public <T extends List<K>, K> T efficientCollectionFetch(InputFunction<T> initial, Class<K> clazz, String... attributePaths) {
		return efficientCollectionFetch(initial, (T val) -> val, clazz, attributePaths);
	}

	public <T, K> T efficientCollectionFetch(InputFunction<T> initial, ValueFetcher<T, K> fetcher, Class<K> clazz, String... attributePaths) {
		List<PropertyNode> propertyNodes = getGenericPropertyWrappers(clazz, attributePaths);
		T initialItem = calculateParentNodeData(initial, propertyNodes);
		List<K> data = fetcher.getValue(initialItem);
		processSubTrees(propertyNodes, data, "");
		return initialItem;
	}


	// TODO: Put in separate class/service because we want this class to be "clean"
	//  (only have methods for using functionality, not functions for internal use)
	public <K> void processSubTrees(List<PropertyNode> propertyNodes, List<K> data, String currentPath) {
		if (propertyNodes.stream().anyMatch(val -> !val.isCompleted())) {
			getCurrentLevel(propertyNodes, currentPath)
					.stream().filter(node -> !node.isCompleted())
					.filter(PropertyNode::isOneToMany).forEach(node -> {
				try {
					rqlMainProcessingUnit.process(data, node, propertyNodes);
				} catch (NoSuchMethodException | IllegalAccessException e) {
					// TODO: Implement proper error-handling
					e.printStackTrace();
				}
			});
		}
	}

	// TODO: Separate in separate class/helper instead of this service
	private <T> T calculateParentNodeData(InputFunction<T> initial, List<PropertyNode> propertyNodes) {
		List<PropertyNode> currentTree = getCurrentLevel(propertyNodes, "")
				.stream().filter(val -> !val.isOneToMany() && !val.isManyToMany()).collect(Collectors.toList());
		List<String> paths = currentTree.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());

		EntityGraph graph = paths.isEmpty() ? EntityGraphs.empty() :
				EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0]));
		T initialItem = initial.accept(graph);

		propertyNodes.forEach(el -> completeNode(new PropertyNode(), currentTree, el));
		return initialItem;
	}

}

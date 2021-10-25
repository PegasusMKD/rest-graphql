package spring.graphql.rest.rql.core.internal;

import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.core.processing.RQLMainProcessingUnit;
import spring.graphql.rest.rql.core.utility.GenericsUtility;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.stream.Collectors;

import static spring.graphql.rest.rql.core.utility.GraphUtility.getCurrentValidPartition;

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

		getCurrentValidPartition(propertyNodes, currentPath)
				.stream().filter(node -> !node.isCompleted())
				.filter(PropertyNode::isOneToMany).forEach(node -> {
					try {
						rqlMainProcessingUnit.process(findProperParents(parents, node, currentPath), node, propertyNodes);
					} catch (NoSuchMethodException | IllegalAccessException e) {
//						TODO: Implement proper error-handling
						e.printStackTrace();
					}
				});
	}

	/**
	 * Method to find the sub-elements of the parents.
	 * <br/>
	 * This is needed for situations like having the entity (Post) as the base, and querying comments.post.comments
	 * (comments.post gets fetched in one query, so the next "in-line" are the comments as parents, while we need the posts).
	 */
	private <K> List<?> findProperParents(List<K> parents, PropertyNode nextNode, String currentPath) throws NoSuchMethodException, IllegalAccessException {
		if (nextNode.getParentPropertyPath().equals("") || currentPath.equals(nextNode.getParentPropertyPath()))
			return parents;

		String leftoverPath = nextNode.getParentPropertyPath().substring(currentPath.length() + 1);
		String[] properties = leftoverPath.split("\\.");

		List<?> latestParents = parents;
		for (String property : properties) {
			Class<?> parentType = latestParents.get(0).getClass();
			Class<?> childType = GenericsUtility.findActualChildType(parentType, property);
			MethodHandle getter = GenericsUtility.findGetter(parentType, childType, property);

			latestParents = latestParents.stream()
					.map(item -> GenericsUtility.invokeHandle(childType, getter, item))
					.collect(Collectors.toList());
		}

		return latestParents;
	}

}

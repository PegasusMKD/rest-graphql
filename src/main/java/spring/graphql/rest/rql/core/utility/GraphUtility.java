package spring.graphql.rest.rql.core.utility;

import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.core.nodes.PropertyNodeTraversal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static spring.graphql.rest.rql.core.utility.NodeUtility.createBasePropertyNode;

public abstract class GraphUtility {

	/**
	 * Transform all the attribute paths from strings to property nodes, and then create
	 * their children property nodes.
	 */
	public static List<PropertyNode> createPropertyNodes(Class<?> parentType, String[] attributePaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));
		List<Field> properties = Arrays.asList(parentType.getDeclaredFields());

		List<PropertyNode> propertyNodes = paths.stream().map(path ->
				createBasePropertyNode(properties, path)).collect(Collectors.toList());

		PropertyNodeTraversal.addAndTraverseProperties(parentType,
				propertyNodes, "", new ArrayList<>(), true);

		return propertyNodes.stream().distinct().collect(Collectors.toList());
	}


	public static List<PropertyNode> getCurrentValidPartition(List<PropertyNode> nodes, String parentPropertyPath) {
		List<PropertyNode> elementalLevel = nodes.stream()
				.filter(node -> node.getParentPropertyPath().equals(parentPropertyPath))
				.collect(Collectors.toList());

		final ArrayList<PropertyNode> allNodes = new ArrayList<>(elementalLevel);
		elementalLevel.stream().filter(PropertyNode::isXToOne)
				.forEach(node -> allNodes.addAll(getCurrentValidPartition(nodes, node.getGraphPath())));

		return allNodes;
	}


	public static List<PropertyNode> getSubPartition(List<PropertyNode> partition, PropertyNode node) {
		return partition.stream()
				.filter(val -> !val.isCompleted())
				.filter(val -> val.getParentPropertyPath().startsWith(node.getGraphPath()))
				.collect(Collectors.toList());
	}

	public static void completeNode(PropertyNode node, List<PropertyNode> currentTree, PropertyNode el) {
		if (currentTree.contains(el) || el.getGraphPath().equals(node.getGraphPath())) {
			el.setCompleted(true);
		}
	}


	public static List<String> getProcessedPaths(List<PropertyNode> currentTree, PropertyNode node) {
		return getGraphPaths(currentTree).stream()
				.filter(path -> path.contains(node.getGraphPath()))
				.map(path -> backtrack(path, node.getGraphPath()))
				.collect(Collectors.toList());
	}

	public static String backtrack(String path, String parentPath) {
		return path.substring(parentPath.length() + 1);
	}

	public static List<String> getGraphPaths(List<PropertyNode> propertyNodes) {
		return propertyNodes.stream()
				.map(PropertyNode::getGraphPath)
				.distinct().collect(Collectors.toList());
	}

}

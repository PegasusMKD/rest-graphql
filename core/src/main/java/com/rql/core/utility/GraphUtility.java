package com.rql.core.utility;

import com.rql.core.nodes.PropertyNode;
import com.rql.core.nodes.PropertyNodeTraversal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GraphUtility {

	private final NodeUtility nodeUtility;
	private final PropertyNodeTraversal propertyNodeTraversal;

	/**
	 * Transform all the attribute paths from strings to property nodes, and then create
	 * their children property nodes.
	 */
	public List<PropertyNode> createPropertyNodes(Class<?> parentType, String[] attributePaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));
		List<Field> properties = Arrays.asList(parentType.getDeclaredFields());

		List<PropertyNode> propertyNodes = paths.stream().map(path ->
				nodeUtility.createBasePropertyNode(properties, path)).collect(Collectors.toList());

		propertyNodeTraversal.addAndTraverseProperties(parentType,
				propertyNodes, "", new ArrayList<>(), true);

		return propertyNodes.stream().distinct().collect(Collectors.toList());
	}


	public List<PropertyNode> getCurrentValidPartition(List<PropertyNode> nodes, String parentPropertyPath) {
		List<PropertyNode> elementalLevel = nodes.stream()
				.filter(node -> node.getParentPropertyPath().equals(parentPropertyPath))
				.toList();

		final ArrayList<PropertyNode> allNodes = new ArrayList<>(elementalLevel);
		elementalLevel.stream().filter(PropertyNode::isXToOne)
				.forEach(node -> allNodes.addAll(getCurrentValidPartition(nodes, node.getGraphPath())));

		return allNodes;
	}


	public List<PropertyNode> getSubPartition(List<PropertyNode> partition, PropertyNode node) {
		return partition.stream()
				.filter(val -> !val.isCompleted())
				.filter(val -> val.getParentPropertyPath().startsWith(node.getGraphPath()))
				.collect(Collectors.toList());
	}

	public void completeNode(PropertyNode node, List<PropertyNode> currentTree, PropertyNode el) {
		if (currentTree.contains(el) || el.getGraphPath().equals(node.getGraphPath())) {
			el.setCompleted(true);
		}
	}


	public List<String> getProcessedPaths(List<PropertyNode> currentTree, PropertyNode node) {
		return getGraphPaths(currentTree).stream()
				.filter(path -> path.contains(node.getGraphPath()))
				.map(path -> backtrack(path, node.getGraphPath()))
				.collect(Collectors.toList());
	}

	public String backtrack(String path, String parentPath) {
		return path.substring(parentPath.length() + 1);
	}

	public List<String> getGraphPaths(List<PropertyNode> propertyNodes) {
		return propertyNodes.stream()
				.map(PropertyNode::getGraphPath)
				.distinct().collect(Collectors.toList());
	}

}

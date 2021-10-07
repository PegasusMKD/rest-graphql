package spring.graphql.rest.nonoptimized.core.nodes;

import java.lang.reflect.Field;
import java.util.List;

import static spring.graphql.rest.nonoptimized.core.nodes.PropertyNodeTraversal.addAndTraverseProperties;

public class NodeUtility {

	public static boolean isPropertyRequested(List<String> propertyGraphPaths, String currentPath, Field field) {
		return propertyGraphPaths.contains(currentPath + field.getName());
	}

	public static void createAndTraversePropertyNode(Class<?> type, List<PropertyNode> properties, String currentPath, List<Class<?>> visited, boolean first, Field property) {
		createAndTraversePropertyNode(type, properties, currentPath, visited, first, property, false);
	}

	public static void createAndTraversePropertyNode(Class<?> type, List<PropertyNode> properties, String currentPath,
													 List<Class<?>> visited, boolean first, Field property, boolean oneToMany) {
		// Generate tree path
		String tmpPath = first ? property.getName() : currentPath + "." + property.getName();

		// Create and add object for holding the node data
		properties.add(new PropertyNode(currentPath, property.getName(), tmpPath.trim(), oneToMany));

		// Iterate children recursively
		addAndTraverseProperties(type, properties, tmpPath, visited, false);
	}

}

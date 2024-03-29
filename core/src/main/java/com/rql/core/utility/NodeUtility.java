package com.rql.core.utility;

import com.rql.core.nodes.PropertyNode;
import com.rql.core.nodes.PropertyNodeTraversal;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;


@Component
@RequiredArgsConstructor
public class NodeUtility {

	private final PropertyNodeTraversal propertyNodeTraversal;

	public boolean isPropertyRequested(List<String> propertyGraphPaths, String currentPath, Field field) {
		return propertyGraphPaths.contains(currentPath + field.getName());
	}

	// TODO(Many-To-Many): Check whether we should just check "isCollection" instead of separating these two relations like this
	public void createAndTraverseXToOnePropertyNode(Class<?> type, List<PropertyNode> properties, String currentPath, List<Class<?>> visited, boolean first, Field property) {
		createAndTraversePropertyNode(type, properties, currentPath, visited, first, property, false, true);
	}

	public void createAndTraverseOneToManyPropertyNode(Class<?> type, List<PropertyNode> properties, String currentPath, List<Class<?>> visited, boolean first, Field property) {
		createAndTraversePropertyNode(type, properties, currentPath, visited, first, property, true, false);
	}

	private void createAndTraversePropertyNode(Class<?> type, List<PropertyNode> properties, String currentPath,
													  List<Class<?>> visited, boolean first, Field property, boolean oneToMany, boolean xToOne) {
		// Generate tree path
		String tmpPath = first ? property.getName() : currentPath + "." + property.getName();

		// Create and add object for holding the node data
		properties.add(new PropertyNode(currentPath, property.getName(), tmpPath.trim(), oneToMany, xToOne));

		// Iterate children recursively
		propertyNodeTraversal.addAndTraverseProperties(type, properties, tmpPath, visited, false);
	}

	/**
	 * Transform an attribute path from a string to a property node
	 */
	public PropertyNode createBasePropertyNode(List<Field> properties, String property) {
		// TODO: Fix so that it properly sees that it's a child node, not a parent node
		if (property.contains(".")) {
			return new PropertyNode(property.substring(0, property.lastIndexOf(".")),
					property.substring(property.lastIndexOf(".") + 1), property,
					property.endsWith("s"), !property.endsWith("s"));
		}

		Field field = properties.stream().filter(prop -> prop.getName().equals(property)).findAny()
				.orElseThrow(() -> new RuntimeException("Path does not exist!"));
		boolean hasOneToMany = field.getAnnotation(OneToMany.class) != null || field.getAnnotation(ManyToMany.class) != null;

		return new PropertyNode("", property, property, hasOneToMany, !hasOneToMany);
	}

}

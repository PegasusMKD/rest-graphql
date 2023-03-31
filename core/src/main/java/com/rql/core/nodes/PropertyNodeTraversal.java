package com.rql.core.nodes;

import com.rql.core.utility.GenericsUtility;
import com.rql.core.utility.GraphUtility;
import com.rql.core.utility.NodeUtility;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PropertyNodeTraversal {
	private final NodeUtility nodeUtility;
	private final GraphUtility graphUtility;
	private final PropertyNodeFilters propertyNodeFilters;
	private final GenericsUtility genericsUtility;

	@Lazy
	public PropertyNodeTraversal(NodeUtility nodeUtility, GraphUtility graphUtility, PropertyNodeFilters propertyNodeFilters, GenericsUtility genericsUtility) {
		this.nodeUtility = nodeUtility;
		this.graphUtility = graphUtility;
		this.propertyNodeFilters = propertyNodeFilters;
		this.genericsUtility = genericsUtility;
	}

	// TODO(Threading): Make each "forEach" a special Thread
	public <T> void addAndTraverseProperties(Class<T> _class, List<PropertyNode> properties, String currentPath, List<Class<?>> visited, boolean first) {
		final List<String> graphPaths = graphUtility.getGraphPaths(properties);

		// X-To-One traversal (Many-To-One, One-To-One)
		Arrays.stream(_class.getDeclaredFields()).filter(property ->
				propertyNodeFilters.filterEagerAndRequiredOneToOne(graphPaths, currentPath, first, property) ||
						propertyNodeFilters.filterEagerAndRequiredManyToOne(graphPaths, currentPath, first, property)
		).forEach(property -> nodeUtility.createAndTraverseXToOnePropertyNode(property.getType(), properties, currentPath, visited, first, property));


		// One-To-Many Traversal
		Arrays.stream(_class.getDeclaredFields()).filter(property -> propertyNodeFilters.filterEagerAndRequiredOneToMany(graphPaths, currentPath, first, property))
				.forEach(property -> nodeUtility.createAndTraverseOneToManyPropertyNode(genericsUtility.findActualChildType(property), properties, currentPath, visited, first, property));
	}

}

package spring.graphql.rest.rql.core.nodes;

import spring.graphql.rest.rql.core.utility.GenericsUtility;
import spring.graphql.rest.rql.core.utility.GraphUtility;
import spring.graphql.rest.rql.core.utility.NodeUtility;

import java.util.Arrays;
import java.util.List;

import static spring.graphql.rest.rql.core.nodes.PropertyNodeFilters.*;
import static spring.graphql.rest.rql.core.utility.NodeUtility.createAndTraverseOneToManyPropertyNode;

public abstract class PropertyNodeTraversal {

	// TODO(Threading): Make each "forEach" a special Thread
	public static <T> void addAndTraverseProperties(Class<T> _class, List<PropertyNode> properties, String currentPath, List<Class<?>> visited, boolean first) {
		final List<String> graphPaths = GraphUtility.getGraphPaths(properties);

		// X-To-One traversal (Many-To-One, One-To-One)
		Arrays.stream(_class.getDeclaredFields()).filter(property ->
				filterEagerAndRequiredOneToOne(graphPaths, currentPath, first, property) ||
						filterEagerAndRequiredManyToOne(graphPaths, currentPath, first, property)
		).forEach(property -> NodeUtility.createAndTraverseXToOnePropertyNode(property.getType(), properties, currentPath, visited, first, property));


		// One-To-Many Traversal
		Arrays.stream(_class.getDeclaredFields()).filter(property -> filterEagerAndRequiredOneToMany(graphPaths, currentPath, first, property))
				.forEach(property -> createAndTraverseOneToManyPropertyNode(GenericsUtility.findActualChildType(property), properties, currentPath, visited, first, property));
	}

}

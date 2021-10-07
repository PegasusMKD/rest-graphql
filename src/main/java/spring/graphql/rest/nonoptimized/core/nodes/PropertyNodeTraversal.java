package spring.graphql.rest.nonoptimized.core.nodes;

import spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static spring.graphql.rest.nonoptimized.core.helpers.GenericsHelper.getActualTypeArgument;
import static spring.graphql.rest.nonoptimized.core.nodes.NodeUtility.createAndTraversePropertyNode;
import static spring.graphql.rest.nonoptimized.core.nodes.PropertyNodeFilters.*;

public abstract class PropertyNodeTraversal {

	// TODO(Threading): Make each "forEach" a special Thread
	public static <T> void addAndTraverseProperties(Class<T> _class, List<PropertyNode> properties, String currentPath, List<Class<?>> visited, boolean first) {
		final List<String> graphPaths = GraphHelpers.getGraphPaths(properties);
		Stream<Field> declaredFields = Arrays.stream(_class.getDeclaredFields());

		// X-To-One traversal (Many-To-One, One-To-One)
		declaredFields.filter(property ->
				filterEagerAndRequiredOneToOne(graphPaths, currentPath, first, property) ||
						filterEagerAndRequiredManyToOne(graphPaths, currentPath, first, property)
		).forEach(property -> createAndTraversePropertyNode(property.getType(), properties, currentPath, visited, first, property));


		// One-To-Many Traversal
		declaredFields.filter(property -> filterEagerAndRequiredOneToMany(graphPaths, currentPath, first, property))
				.forEach(property -> createAndTraversePropertyNode(getActualTypeArgument(property), properties, currentPath, visited, first, property, true));
	}

}

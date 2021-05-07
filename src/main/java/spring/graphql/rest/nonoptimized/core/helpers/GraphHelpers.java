package spring.graphql.rest.nonoptimized.core.helpers;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import spring.graphql.rest.nonoptimized.core.PropertyNodeTraversal;
import spring.graphql.rest.nonoptimized.core.PropertyNode;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GraphHelpers {

	public static List<PropertyNode> getGenericPropertyWrappers(Class<?> _clazz, String[] attributePaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));
		List<Field> fields = Arrays.asList(_clazz.getDeclaredFields());

		List<PropertyNode> propertyWrappers = paths.stream().map(val ->
				createPropertyNode(fields, val)).collect(Collectors.toList());

		PropertyNodeTraversal.getDefaultSubAttributes(_clazz,
				propertyWrappers, "", new ArrayList<>(), true);

		return propertyWrappers.stream().distinct().collect(Collectors.toList());
	}

	private static PropertyNode createPropertyNode(List<Field> fields, String val) {
		// TODO: Fix so that it properly sees that it's a child node, not a parent node
		if(val.contains(".")) {
			return new PropertyNode("", val, val, false);
		}

		Field field = fields.stream().filter(prop -> prop.getName().equals(val)).findAny()
				.orElseThrow(() -> new RuntimeException("Path does not exist!"));
		boolean hasOneToMany = field.getAnnotation(OneToMany.class) != null || field.getAnnotation(ManyToMany.class) != null;

		return new PropertyNode("", val, val, hasOneToMany);
	}

	public static List<String> getPaths(List<PropertyNode> propertyNodes) {
		return propertyNodes.stream().map(PropertyNode::getGraphPath).distinct().collect(Collectors.toList());
	}

	public static String lowerPath(String path, String parentPath) {
		return path.substring(parentPath.length() + 1);
	}

	public static EntityGraph getEntityGraph(List<String> paths) {
		return paths.isEmpty() ? EntityGraphs.empty() :
				EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0]));
	}

	public static List<PropertyNode> getSubTree(List<PropertyNode> tree, PropertyNode node) {
		return tree.stream()
				.filter(val -> !val.isCompleted())
				.filter(val -> val.getParentPropertyPath().contains(node.getProperty()))
				.collect(Collectors.toList());
	}

	public static void completeNode(PropertyNode node, List<PropertyNode> currentTree, PropertyNode el) {
		if(currentTree.contains(el) || el.getGraphPath().equals(node.getGraphPath())) {
			el.setCompleted(true);
		}
	}

	public static List<String> getProcessedPaths(List<PropertyNode> currentTree, PropertyNode node) {
		List<String> paths = getPaths(currentTree).stream().filter(val -> val.contains(node.getGraphPath())).collect(Collectors.toList());
		return paths.stream().map(p -> lowerPath(p, node.getGraphPath())).collect(Collectors.toList());
	}
}

package spring.graphql.rest.nonoptimized.core.nodes;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GenericsHelper.getActualTypeArgument;

public abstract class PropertyNodeTraversal {

	public static <T> void getDefaultSubAttributes(Class<T> _class, List<PropertyNode> properties, String currentPath, List<Class<?>> passedClasses, boolean first) {

		// One-To-One traversal
		List<Field> oneToOneProperties = Arrays.stream(_class.getDeclaredFields())
				.filter(field -> filterEagerAndRequiredOneToOne(properties, currentPath, first, field)).collect(Collectors.toList());
		oneToOneProperties.forEach(prop -> traverseProperties(prop.getType(), properties, currentPath, passedClasses, first, prop));


		// One-To-Many Traversal
		List<Field> oneToManyProperties = Arrays.stream(_class.getDeclaredFields())
				.filter(field -> filterEagerAndRequiredOneToMany(properties, currentPath, first, field))
				.collect(Collectors.toList());

		oneToManyProperties.forEach(prop ->
				traverseProperties(getActualTypeArgument(prop), properties, currentPath, passedClasses, first, prop, true)
		);

		// Many-To-One Traversal
		List<Field> manyToOneProperties = Arrays.stream(_class.getDeclaredFields())
				.filter(field -> filterEagerAndRequiredManyToOne(properties, currentPath, first, field))
				.collect(Collectors.toList());

		manyToOneProperties.forEach(prop -> traverseProperties(prop.getType(), properties, currentPath, passedClasses, first, prop));
	}

	private static boolean filterEagerAndRequiredManyToOne(List<PropertyNode> properties, String currentPath, boolean first, Field field) {
		ManyToOne mainAnnotation = field.getAnnotation(ManyToOne.class);
		JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
		return mainAnnotation != null &&
				((first && requestedField(properties, "", field)) || !mainAnnotation.optional() ||
						(joinColumnAnnotation != null && !joinColumnAnnotation.nullable()) ||
						(!first && mainAnnotation.fetch() != FetchType.LAZY ||
								!Arrays.equals(mainAnnotation.cascade(), new CascadeType[]{}) ||
								requestedField(properties, currentPath + ".", field)));
	}

	private static boolean filterEagerAndRequiredOneToMany(List<PropertyNode> properties, String currentPath, boolean first, Field field) {
		OneToMany mainAnnotation = field.getAnnotation(OneToMany.class);
		return mainAnnotation != null &&
				((first && requestedField(properties, "", field))
						|| (!first && (mainAnnotation.fetch() != FetchType.LAZY ||
						requestedField(properties, currentPath + ".", field))));
	}

	private static boolean filterEagerAndRequiredOneToOne(List<PropertyNode> properties, String currentPath, boolean first, Field field) {
		OneToOne mainAnnotation = field.getAnnotation(OneToOne.class);
		return mainAnnotation != null &&
				((first && (!mainAnnotation.mappedBy().isEmpty() || requestedField(properties, "", field))) ||
						(!first && !mainAnnotation.mappedBy().isEmpty() || requestedField(properties, currentPath + ".", field)));
	}

	private static boolean requestedField(List<PropertyNode> properties, String currentPath, Field field) {
		return properties.stream().map(PropertyNode::getGraphPath)
				.collect(Collectors.toList()).contains(currentPath + field.getName());
	}


	private static void traverseProperties(Class<?> type, List<PropertyNode> properties, String currentPath,
										   List<Class<?>> passedClasses, boolean first, Field field) {
		traverseProperties(type, properties, currentPath, passedClasses, first, field, false);
	}

	private static void traverseProperties(Class<?> type, List<PropertyNode> properties, String currentPath,
										   List<Class<?>> passedClasses, boolean first, Field field, boolean oneToMany) {
		// Generate tree path
		String tmpPath = first ? field.getName() : currentPath + "." + field.getName();

		// Create class for holding the node
		PropertyNode propWrapper = new PropertyNode();
		propWrapper.setParentPropertyPath(currentPath);
		propWrapper.setProperty(field.getName());
		propWrapper.setGraphPath(tmpPath.trim());
		propWrapper.setOneToMany(oneToMany);

		// Add node
		properties.add(propWrapper);

		// Iterate recursively
		getDefaultSubAttributes(type, properties, tmpPath, passedClasses, false);
	}
}

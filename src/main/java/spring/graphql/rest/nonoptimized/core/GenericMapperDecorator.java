package spring.graphql.rest.nonoptimized.core;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class GenericMapperDecorator {

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
		return field.getAnnotation(ManyToOne.class) != null &&
				((first && properties.stream().map(PropertyNode::getGraphPath)
						.collect(Collectors.toList()).contains(field.getName())) || !field.getAnnotation(ManyToOne.class).optional() ||
						(!first && field.getAnnotation(ManyToOne.class).fetch() != FetchType.LAZY ||
								!Arrays.equals(field.getAnnotation(ManyToOne.class).cascade(), new CascadeType[]{}) ||
								properties.stream().map(PropertyNode::getGraphPath)
										.collect(Collectors.toList()).contains(currentPath + "." + field.getName())));
	}

	private static boolean filterEagerAndRequiredOneToMany(List<PropertyNode> properties, String currentPath, boolean first, Field field) {
		return field.getAnnotation(OneToMany.class) != null &&
				((first && properties.stream().map(PropertyNode::getGraphPath)
						.collect(Collectors.toList()).contains(field.getName()))
						|| (!first && (field.getAnnotation(OneToMany.class).fetch() != FetchType.LAZY ||
						properties.stream().map(PropertyNode::getGraphPath)
								.collect(Collectors.toList()).contains(currentPath + "." + field.getName()))));
	}

	private static boolean filterEagerAndRequiredOneToOne(List<PropertyNode> properties, String currentPath, boolean first, Field field) {
		return field.getAnnotation(OneToOne.class) != null &&
				((first && (!field.getAnnotation(OneToOne.class).mappedBy().isEmpty() || properties.stream()
						.map(PropertyNode::getGraphPath).collect(Collectors.toList()).contains(field.getName())))
						|| (!first && !field.getAnnotation(OneToOne.class).mappedBy().isEmpty() || properties.stream()
						.map(PropertyNode::getGraphPath).collect(Collectors.toList()).contains(currentPath + "." + field.getName())));
	}

	private static Class<?> getActualTypeArgument(Field val) {
		return (Class<?>) ((ParameterizedType) val.getGenericType()).getActualTypeArguments()[0];
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

	private static String capitalize(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

}

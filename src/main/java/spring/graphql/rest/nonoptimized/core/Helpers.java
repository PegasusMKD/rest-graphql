package spring.graphql.rest.nonoptimized.core;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Helpers {

	public static List<PropertyNode> getGenericPropertyWrappers(Class<?> _clazz, String[] attributePaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));
		List<Field> fields = Arrays.asList(_clazz.getDeclaredFields());

		List<PropertyNode> propertyWrappers = paths.stream().map(val ->
				createPropertyNode(fields, val)).collect(Collectors.toList());

		GenericMapperDecorator.getDefaultSubAttributes(_clazz,
				propertyWrappers, "", new ArrayList<>(), true);

		return propertyWrappers.stream().distinct().collect(Collectors.toList());
	}

	private static PropertyNode createPropertyNode(List<Field> fields, String val) {
		Field field = fields.stream().filter(prop -> prop.getName().equals(val)).findAny()
				.orElseThrow(() -> new RuntimeException("Path does not exist!"));
		boolean hasOneToMany = field.getAnnotation(OneToMany.class) != null || field.getAnnotation(ManyToMany.class) != null;

		return new PropertyNode("", val, val, hasOneToMany);
	}
}

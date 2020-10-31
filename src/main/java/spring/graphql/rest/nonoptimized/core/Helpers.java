package spring.graphql.rest.nonoptimized.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Helpers {

	public static List<GenericPropertyWrapper> getGenericPropertyWrappers(Class<?> _clazz, String[] attributePaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));

		List<GenericPropertyWrapper> propertyWrappers = paths.stream().map(val ->
				new GenericPropertyWrapper("", val, val)).collect(Collectors.toList());

		GenericMapperDecorator.getDefaultSubAttributes(_clazz,
				propertyWrappers, "", new ArrayList<>(), true);

		return propertyWrappers.stream().distinct().collect(Collectors.toList());
	}
}

package spring.graphql.rest.nonoptimized;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

public class GenericsHelper {

	public static Class<?> getActualTypeArgument(Field val) {
		return Collection.class.isAssignableFrom(val.getType()) ? (Class<?>) ((ParameterizedType) val.getGenericType()).getActualTypeArguments()[0] : val.getType();
	}
}

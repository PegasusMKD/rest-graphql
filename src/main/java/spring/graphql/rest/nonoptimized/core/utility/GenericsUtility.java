package spring.graphql.rest.nonoptimized.core.utility;

import spring.graphql.rest.nonoptimized.core.dto.ChildType;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

public abstract class GenericsUtility {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

	public static Class<?> findActualChildType(Field val) {
		return Collection.class.isAssignableFrom(val.getType()) ?
				(Class<?>) ((ParameterizedType) val.getGenericType()).getActualTypeArguments()[0] :
				val.getType();
	}

	// TODO(Opinionated): Stop forcing a String ID
	public static MethodHandle findIdGetter(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException {
		return LOOKUP.findVirtual(clazz, "getId", MethodType.methodType(String.class));
	}

	public static MethodHandle findGetter(Class<?> clazz, Class<?> resultType, String property) throws NoSuchMethodException, IllegalAccessException {
		return LOOKUP.findVirtual(clazz, "get" + GeneralUtility.capitalize(property), MethodType.methodType(resultType));
	}

	public static ChildType findChildTypeAndParentAccess(PropertyNode node, Class<?> parentClass) {
		Field property = Arrays.stream(parentClass.getDeclaredFields())
				.filter(field -> field.getName().equals(node.getProperty()))
				.findAny().orElseThrow(() -> new RuntimeException("Property doesn't exist!"));

		return new ChildType(GenericsUtility.findActualChildType(property),
				property.getAnnotation(OneToMany.class) != null ? property.getAnnotation(OneToMany.class).mappedBy()
						: property.getAnnotation(ManyToMany.class).mappedBy());
	}

	public static Class<?> findActualChildType(Class<?> parentClass, String property) {
		return GenericsUtility.findActualChildType(Arrays.stream(parentClass.getDeclaredFields())
				.filter(field -> field.getName().equals(property))
				.findAny().orElseThrow(() -> new RuntimeException("Property doesn't exist!")));
	}

	public static <T, K> K invokeHandle(Class<K> resultType, MethodHandle handle, T val) {
		try {
			return resultType.cast(handle.invoke(val));
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			return null;
		}
	}
}

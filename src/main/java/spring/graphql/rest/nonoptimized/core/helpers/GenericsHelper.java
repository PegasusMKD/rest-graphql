package spring.graphql.rest.nonoptimized.core.helpers;

import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.example.processors.dto.ClassAndPropertyDto;

import javax.persistence.OneToMany;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

public class GenericsHelper {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

	public static Class<?> getActualTypeArgument(Field val) {
		return Collection.class.isAssignableFrom(val.getType()) ? (Class<?>) ((ParameterizedType) val.getGenericType()).getActualTypeArguments()[0] : val.getType();
	}

	public static MethodHandle findIdMethod(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException {
		return LOOKUP.findVirtual(clazz, "getId", MethodType.methodType(String.class));
	}

	public static ClassAndPropertyDto findChildClass(PropertyNode node, Class<?> parentClass) {
		Field prop = Arrays.stream(parentClass.getDeclaredFields())
				.filter(field -> field.getName().equals(node.getProperty()))
				.findAny().orElseThrow(() -> new RuntimeException("Property doesn't exist!"));

		return new ClassAndPropertyDto(GenericsHelper.getActualTypeArgument(prop), prop.getAnnotation(OneToMany.class).mappedBy());
	}

	public static <T> String invokeHandle(MethodHandle handle, T val) {
		try {
			return (String) handle.invoke(val);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			return null;
		}
	}
}

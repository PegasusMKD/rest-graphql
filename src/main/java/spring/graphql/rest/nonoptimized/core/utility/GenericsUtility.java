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

	public static Class<?> findChildType(Field val) {
		return Collection.class.isAssignableFrom(val.getType()) ?
				(Class<?>) ((ParameterizedType) val.getGenericType()).getActualTypeArguments()[0] :
				val.getType();
	}

	// TODO(Opinionated): Stop forcing a String ID
	public static MethodHandle findIdGetter(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException {
		return LOOKUP.findVirtual(clazz, "getId", MethodType.methodType(String.class));
	}

	public static ChildType findChildTypeAndParentAccess(PropertyNode node, Class<?> parentClass) {
		Field prop = Arrays.stream(parentClass.getDeclaredFields())
				.filter(field -> field.getName().equals(node.getProperty()))
				.findAny().orElseThrow(() -> new RuntimeException("Property doesn't exist!"));

		return new ChildType(GenericsUtility.findChildType(prop),
				prop.getAnnotation(OneToMany.class) != null ? prop.getAnnotation(OneToMany.class).mappedBy()
						: prop.getAnnotation(ManyToMany.class).mappedBy());
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

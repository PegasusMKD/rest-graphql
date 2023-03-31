package com.rql.core.utility;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.jetbrains.annotations.NotNull;
import com.rql.core.dto.ChildType;
import com.rql.core.nodes.PropertyNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
				.findAny().orElseThrow(() -> new RuntimeException("Property doesn't exist!" + property)));
	}

	public static <T, K> K invokeHandle(Class<K> resultType, MethodHandle handle, T val) {
		try {
			return resultType.cast(handle.invoke(val));
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			throw new RuntimeException();
		}
	}

	/**
	 * Find all the possible getters for a property.
	 * <br/><br/>
	 * This is needed due to HibernateProxy getters, which would return a different object compared to T,
	 * so we need to have getters for both situations.
	 *
	 * @param children       List of all the child data
	 * @param parents        List of all the parents
	 * @param parentProperty Property to which the appropriate child data should be set
	 * @param <T>            Type of parents
	 */
	@NotNull
	public static <T> HashMap<Class<?>, MethodHandle> mapParentHandlers(List<T> parents, List<?> children, String parentProperty) {
		HashMap<Class<?>, MethodHandle> parentHandlers = new HashMap<>();
		children.stream().map(Object::getClass).distinct().forEach(_clazz -> {
			try {
				parentHandlers.putIfAbsent(_clazz, GenericsUtility.findGetter(_clazz, parents.get(0).getClass(), parentProperty));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		return parentHandlers;
	}
}

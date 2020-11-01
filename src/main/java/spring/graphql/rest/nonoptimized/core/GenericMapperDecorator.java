package spring.graphql.rest.nonoptimized.core;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class GenericMapperDecorator {

	public static <T> void getDefaultSubAttributes(Class<T> _class, List<GenericPropertyWrapper> properties, String currentPath, List<Class<?>> passedClasses, boolean first) {

		// One-To-One traversal
		List<Field> oneToOneObjParameterFields = Arrays.stream(_class.getDeclaredFields())
				.filter(field -> field.getAnnotation(OneToOne.class) != null &&
						((first && (!field.getAnnotation(OneToOne.class).mappedBy().isEmpty() || properties.stream()
								.map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList()).contains(field.getName())))
								|| (!first && !field.getAnnotation(OneToOne.class).mappedBy().isEmpty() || properties.stream()
								.map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList())
								.contains(currentPath + "." + field.getName())))).collect(Collectors.toList());
		for(Field oneToOne: oneToOneObjParameterFields) {
			String tmpPath = first ? oneToOne.getName() : currentPath + "." + oneToOne.getName();
			GenericPropertyWrapper propWrapper = new GenericPropertyWrapper();
			propWrapper.setParentPropertyPath(currentPath);
			propWrapper.setProperty(oneToOne.getName());
			propWrapper.setGraphPath(tmpPath.trim());
			properties.add(propWrapper);
			getDefaultSubAttributes(oneToOne.getType(), properties, tmpPath, passedClasses, false);
		}

		// One-To-Many Traversal
		List<Field> oneToManyObjParameterFields = Arrays.stream(_class.getDeclaredFields())
				.filter(field -> field.getAnnotation(OneToMany.class) != null &&
						((first && properties.stream().map(GenericPropertyWrapper::getGraphPath)
								.collect(Collectors.toList()).contains(field.getName()))
						|| (!first && (field.getAnnotation(OneToMany.class).fetch() != FetchType.LAZY ||
								properties.stream().map(GenericPropertyWrapper::getGraphPath)
								.collect(Collectors.toList()).contains(currentPath + "." + field.getName())))))
				.collect(Collectors.toList());
		for(Field oneToMany: oneToManyObjParameterFields) {
			String tmpPath = first ? oneToMany.getName() : currentPath + "." + oneToMany.getName();
			GenericPropertyWrapper propWrapper = new GenericPropertyWrapper();
			propWrapper.setParentPropertyPath(currentPath);
			propWrapper.setProperty(oneToMany.getName());
			propWrapper.setGraphPath(tmpPath.trim());
			properties.add(propWrapper);
			getDefaultSubAttributes(((Class<?>)((ParameterizedType)oneToMany.getGenericType())
					.getActualTypeArguments()[0]), properties, tmpPath, passedClasses, false);
		}

		// Many-To-One Traversal
		List<Field> manyToOneObjParameterFields = Arrays.stream(_class.getDeclaredFields())
				.filter(field -> field.getAnnotation(ManyToOne.class) != null &&
						((first && properties.stream().map(GenericPropertyWrapper::getGraphPath)
								.collect(Collectors.toList()).contains(field.getName())) || !field.getAnnotation(ManyToOne.class).optional() ||
						(!first && field.getAnnotation(ManyToOne.class).fetch() != FetchType.LAZY ||
								!Arrays.equals(field.getAnnotation(ManyToOne.class).cascade(), new CascadeType[]{}) ||
								properties.stream().map(GenericPropertyWrapper::getGraphPath)
										.collect(Collectors.toList()).contains(currentPath + "." + field.getName()))))
				.collect(Collectors.toList());
		for(Field manyToOne: manyToOneObjParameterFields) {
			String tmpPath = first ? manyToOne.getName() : currentPath + "." + manyToOne.getName();
			GenericPropertyWrapper propWrapper = new GenericPropertyWrapper();
			propWrapper.setParentPropertyPath(currentPath);
			propWrapper.setProperty(manyToOne.getName());
			propWrapper.setGraphPath(tmpPath.trim());
			properties.add(propWrapper);
			getDefaultSubAttributes(manyToOne.getType(), properties, tmpPath, passedClasses, false);
		}
	}

	public <T, K> void emptyModels(T entity, K dto) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		List<Field> entityColumnFields = Arrays.stream(entity.getClass().getDeclaredFields())
				.filter(field -> field.getAnnotation(JoinColumn.class) != null).collect(Collectors.toList());
		for (Field entityField : entityColumnFields) {
			Method dtoGetter = dto.getClass().getMethod("get" + capitalize(entityField.getName()));
			Method entityGetter = entity.getClass().getMethod("get" + capitalize(entityField.getName()));
			Method entitySetter = entity.getClass().getMethod("set" + capitalize(entityField.getName()), entityGetter.getReturnType());
			Method entityIsIdSet = entityGetter.getReturnType().getMethod("isIdSet");
			Method dtoIsIdSet = dtoGetter.getReturnType().getMethod("isIdSet");

			Object dtoObject = dtoGetter.invoke(dto);
			Object entityObject = entityGetter.invoke(entity);

			if (dtoObject != null && entityObject != null
					&& ((boolean) dtoIsIdSet.invoke(dtoObject)) &&
					((boolean) entityIsIdSet.invoke(entityObject))) {
				Object empty = null;
				entitySetter.invoke(entity, empty);
			}
		}

	}

	private static String capitalize(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

}

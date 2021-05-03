package spring.graphql.rest.nonoptimized.example.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.GenericsHelper;
import spring.graphql.rest.nonoptimized.core.Helpers;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.Identifiable;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnitDistributor;
import spring.graphql.rest.nonoptimized.example.models.Account;
import spring.graphql.rest.nonoptimized.example.service.AccountService;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RQLMainProcessingUnit {

	public static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private final RQLProcessingUnitDistributor processingUnitDistributor;

	private Logger logger = LoggerFactory.getLogger(RQLMainProcessingUnit.class);

	public RQLMainProcessingUnit(RQLProcessingUnitDistributor processingUnitDistributor) {
		this.processingUnitDistributor = processingUnitDistributor;
	}

	public <T> List<T> process(List<T> data, PropertyNode node, List<PropertyNode> tree) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
		long startTime = System.nanoTime();
		if (data.size() == 0) {
			return data;
		}
		T sampleElement = data.get(0);
		Class<? extends Object> parentClass = sampleElement.getClass();

		Class<?> childClass = findChildClass(node, parentClass);
		RQLProcessingUnit processingUnit = processingUnitDistributor.findProcessingUnit(childClass);
		Method getId = findIdMethod(parentClass);


		Set<String> dataIds = data.stream().map(val -> {
			try {
				return (String) getId.invoke(val);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toSet());
		long endTime = System.nanoTime();
		logger.info("Preprocess of data: {} ms", (endTime - startTime) / 1000000);

		startTime = System.nanoTime();
		HashMap<String, Object> res = processingUnit.process(tree, dataIds, node);
		endTime = System.nanoTime();
		logger.info("Process of data: {} ms", (endTime - startTime) / 1000000);

		startTime = System.nanoTime();
		if (node.isOneToMany()) {
//			data = ((List<?>) res.get("data")).get(0) instanceof Identifiable && sampleElement instanceof Identifiable ?
//					(List<T>) oneToManyMappingBoosted((List<? extends Identifiable>) data, res, node) : oneToManyMapping(data, res, getId, node);
			data = oneToManyMappingHandle(data, res, node);
		} else if (node.isManyToMany()) {
			data = manyToManyMapping(data, res);
		}
		endTime = System.nanoTime();
		logger.info("Mapping of data: {} ms", (endTime - startTime) / 1000000);

		return data;
	}

	private Method findIdMethod(Class<?> childClass) throws NoSuchMethodException {
		return childClass.getDeclaredMethod("getId");
	}

	// With abstract class for getID
	private <T> List<?> oneToManyMappingBoosted(List<? extends Identifiable> data, HashMap<String, Object> result, PropertyNode node) throws NoSuchMethodException {
		long startTime = System.nanoTime();
		List<?> children = (List<?>) result.get("data");
		String parentProp = (String) result.get("parent");

		if (children == null || children.size() == 0) {
			return data;
		}

		// TODO: Get setter
		Method setter = data.get(0).getClass().getDeclaredMethod("set" + Helpers.capitalize(node.getProperty()), Set.class);

		// TODO: Get getter for property which we are trying to relate (get the parent from the child)
		Method getter = children.get(0).getClass().getDeclaredMethod("get" + Helpers.capitalize(parentProp));
		long endTime = System.nanoTime();
		logger.info("Preparations for mapping: {} ms", (endTime - startTime) / 1000000);


		data.forEach(v -> mapChildrenToParentBoosted(children, setter, getter, v));
		return data;
	}

	private <T extends Identifiable> void mapChildrenToParentBoosted(List<?> children, Method setter, Method getter, T v) {
		try {
			long startTime = System.nanoTime();
			Set<?> fetchedData = (children.stream().filter(p -> findChildrenByParentBoosted(getter, v, p)).collect(Collectors.toSet()));
			setter.invoke(v, fetchedData);
			long endTime = System.nanoTime();
//			logger.info("Filter data: {} ms", (endTime - startTime) / 1000000);

			startTime = System.nanoTime();
			children.removeAll(fetchedData);
			endTime = System.nanoTime();
//			logger.info("Removal of data: {} ms", (endTime - startTime) / 1000000);

		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private <T extends Identifiable> boolean findChildrenByParentBoosted(Method getter, T v, Object p) {
		try {
			return ((Identifiable)getter.invoke(p)).getId().equals(v.getId());
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return false;
	}

	// With pure reflection
	private <T> List<T> oneToManyMapping(List<T> data, HashMap<String, Object> result, Method getId, PropertyNode node) throws NoSuchMethodException {
		long startTime = System.nanoTime();
		List<?> children = (List<?>) result.get("data");
		String parentProp = (String) result.get("parent");

		if (children == null || children.size() == 0) {
			return data;
		}

		// TODO: Get setter
		Method setter = data.get(0).getClass().getDeclaredMethod("set" + Helpers.capitalize(node.getProperty()), Set.class);

		// TODO: Get getter for property which we are trying to relate (get the parent from the child)
		Method getter = children.get(0).getClass().getDeclaredMethod("get" + Helpers.capitalize(parentProp));
		long endTime = System.nanoTime();
		logger.info("Preparations for mapping: {} ms", (endTime - startTime) / 1000000);


		data.forEach(v -> mapChildrenToParent(getId, children, setter, getter, v));
		return data;
	}

	private <T> void mapChildrenToParent(Method getId, List<?> children, Method setter, Method getter, T v) {
		try {
			long startTime = System.nanoTime();
			Set<?> fetchedData = (children.stream().filter(p -> findChildrenByParent(getId, getter, v, p)).collect(Collectors.toSet()));
			setter.invoke(v, fetchedData);
			long endTime = System.nanoTime();
//			logger.info("Filter data: {} ms", (endTime - startTime) / 1000000);

			startTime = System.nanoTime();
			children.removeAll(fetchedData);
			endTime = System.nanoTime();
//			logger.info("Removal of data: {} ms", (endTime - startTime) / 1000000);

		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private <T> boolean findChildrenByParent(Method getId, Method getter, T v, Object p) {
		try {
			return (getId.invoke(getter.invoke(p))).equals(getId.invoke(v));
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return false;
	}

	// With method handles

	private <T> List<T> oneToManyMappingHandle(List<T> data, HashMap<String, Object> result, PropertyNode node) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
		long startTime = System.nanoTime();
		List<?> children = (List<?>) result.get("data");
		String parentProp = (String) result.get("parent");

		if (children == null || children.size() == 0) {
			return data;
		}

		// TODO: Get setter
		MethodHandle setter = LOOKUP.findVirtual(data.get(0).getClass(), "set" + Helpers.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));

		// TODO: Get getter for property which we are trying to relate (get the parent from the child)
		MethodHandle getter = LOOKUP.findVirtual(children.get(0).getClass(), "get" + Helpers.capitalize(parentProp), MethodType.methodType(data.get(0).getClass()));

		// TODO: Get handle for ID
		MethodHandle parentGetId = LOOKUP.findVirtual(data.get(0).getClass(), "getId", MethodType.methodType(String.class));

		long endTime = System.nanoTime();
		logger.info("Preparations for mapping: {} ms", (endTime - startTime) / 1000000);


		data.forEach(v -> mapChildrenToParentHandle(parentGetId, children, setter, getter, v));
		return data;
	}

	private <T> void mapChildrenToParentHandle(MethodHandle parentGetId, List<?> children, MethodHandle setter, MethodHandle getter, T v) {
		try {
			long startTime = System.nanoTime();
			Set<?> fetchedData = (children.stream().filter(p -> findChildrenByParentHandle(parentGetId, getter, v, p)).collect(Collectors.toSet()));
			setter.invoke(v, fetchedData);
			long endTime = System.nanoTime();
//			logger.info("Filter data: {} ms", (endTime - startTime) / 1000000);

			startTime = System.nanoTime();
			children.removeAll(fetchedData);
			endTime = System.nanoTime();
//			logger.info("Removal of data: {} ms", (endTime - startTime) / 1000000);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private <T> boolean findChildrenByParentHandle(MethodHandle parentGetId, MethodHandle getter, T v, Object p) {
		try {
			return (parentGetId.invoke(getter.invoke(p))).equals(parentGetId.invoke(v));
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}


	private <T> List<T> manyToManyMapping(List<T> data, HashMap<String, Object> children) {
		// TODO: Implement mapping
		return data;
	}

	private Class<?> findChildClass(PropertyNode node, Class<?> parentClass) {
		Field prop = Arrays.stream(parentClass.getDeclaredFields())
				.filter(field -> field.getName().equals(node.getProperty()))
				.findAny().orElseThrow(() -> new RuntimeException("Property doesn't exist!"));

		return GenericsHelper.getActualTypeArgument(prop);
	}

}

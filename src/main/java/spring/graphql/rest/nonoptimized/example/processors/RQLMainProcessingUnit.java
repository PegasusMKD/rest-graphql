package spring.graphql.rest.nonoptimized.example.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.Helpers;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnitDistributor;
import spring.graphql.rest.nonoptimized.example.processors.dto.ClassAndPropertyDto;
import spring.graphql.rest.nonoptimized.example.processors.dto.TransferResultDto;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.GenericsHelper.*;

@Service
public class RQLMainProcessingUnit {

	public static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private final RQLProcessingUnitDistributor processingUnitDistributor;

	private Logger logger = LoggerFactory.getLogger(RQLMainProcessingUnit.class);

	public RQLMainProcessingUnit(RQLProcessingUnitDistributor processingUnitDistributor) {
		this.processingUnitDistributor = processingUnitDistributor;
	}

	public <T> void process(List<T> data, PropertyNode node, List<PropertyNode> tree) throws NoSuchMethodException, IllegalAccessException {
		if (data.size() == 0) {
			return;
		}
		Class<?> parentClass = data.get(0).getClass();
		ClassAndPropertyDto classAndProp = findChildClass(node, parentClass);
		Class<?> childClass = classAndProp.getClazz();
		MethodHandle getId = findIdMethod(parentClass);

		RQLProcessingUnit<?> processingUnit = processingUnitDistributor.findProcessingUnit(childClass);

		Set<String> dataIds = data.stream().map(val -> invokeHandle(getId, val)).collect(Collectors.toSet());
		TransferResultDto<?> res = processingUnit.process(tree, dataIds, node, classAndProp.getPropertyToParent());

		if (node.isOneToMany()) {
			oneToManyMappingHandle(data, res, node, getId);
		} else if (node.isManyToMany()) {
			// TODO: Return many to many mappings
		}
	}

	private <T> void oneToManyMappingHandle(List<T> data, TransferResultDto<?> result, PropertyNode node, MethodHandle getId) throws NoSuchMethodException, IllegalAccessException {
		List<?> children = result.getData();
		String parentProp = result.getParent();

		if (children == null || children.size() == 0) {
			return;
		}

		MethodHandle setter = LOOKUP.findVirtual(data.get(0).getClass(), "set" + Helpers.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));
		MethodHandle getParent = LOOKUP.findVirtual(children.get(0).getClass(), "get" + Helpers.capitalize(parentProp), MethodType.methodType(data.get(0).getClass()));

		data.forEach(v -> mapChildrenToParentHandle(getId, children, setter, getParent, v));
	}

	private <T> void mapChildrenToParentHandle(MethodHandle getId, List<?> children, MethodHandle setter, MethodHandle getParent, T v) {
		try {
			Set<?> fetchedData = children.stream().filter(p -> findChildrenByParentHandle(getId, getParent, v, p)).collect(Collectors.toSet());
			setter.invoke(v, fetchedData);
			children.removeAll(fetchedData);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private <T> boolean findChildrenByParentHandle(MethodHandle getId, MethodHandle getParent, T v, Object p) {
		try {
			return (getId.invoke(getParent.invoke(p))).equals(getId.invoke(v));
			// test
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}

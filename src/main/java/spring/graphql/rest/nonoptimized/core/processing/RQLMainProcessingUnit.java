package spring.graphql.rest.nonoptimized.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.dto.ClassAndPropertyDto;
import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.helpers.Helpers;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GenericsHelper.*;

@Service
public class RQLMainProcessingUnit {

	public static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private final RQLProcessingUnitDistributor processingUnitDistributor;

	private final Logger logger = LoggerFactory.getLogger(RQLMainProcessingUnit.class);

	public RQLMainProcessingUnit(RQLProcessingUnitDistributor processingUnitDistributor) {
		this.processingUnitDistributor = processingUnitDistributor;
	}

	@Transactional(readOnly = true)
	public <T> void process(List<T> data, PropertyNode node, List<PropertyNode> tree) throws NoSuchMethodException, IllegalAccessException {
		if (data.size() == 0 || node.isCompleted()) {
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
			// TODO: Return many-to-many mappings
		}
	}

	private <T> void oneToManyMappingHandle(List<T> data, TransferResultDto<?> result, PropertyNode node, MethodHandle getId) throws NoSuchMethodException, IllegalAccessException {
		List<?> children = result.getData();
		String parentProp = result.getParent();

		if (children == null || children.size() == 0) {
			return;
		}
		MethodHandle setChildren = LOOKUP.findVirtual(data.get(0).getClass(), "set" + Helpers.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));

		HashMap<Class<?>, MethodHandle> parentHandlers = new HashMap<>();
		List<Class<?>> classes = children.stream().map(Object::getClass).distinct().collect(Collectors.toList());
		classes.forEach(_clazz -> {
			try {
				parentHandlers.putIfAbsent(_clazz, LOOKUP.findVirtual(_clazz, "get" + Helpers.capitalize(parentProp), MethodType.methodType(data.get(0).getClass())));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});

		data.forEach(v -> mapChildrenToParentHandle(getId, children, setChildren, parentHandlers, v));
	}

	private <T> void mapChildrenToParentHandle(MethodHandle getId, List<?> children, MethodHandle setChildren, HashMap<Class<?>, MethodHandle> parentHandlers, T v) {
		try {
			Set<?> fetchedData = children.stream().filter(p -> findChildrenByParentHandle(getId, parentHandlers, v, p)).collect(Collectors.toSet());
			setChildren.invoke(v, fetchedData);
			children.removeAll(fetchedData);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private <T> boolean findChildrenByParentHandle(MethodHandle getId, HashMap<Class<?>, MethodHandle> parentHandlers, T v, Object p) {
		try {
			return (getId.invoke(parentHandlers.get(p.getClass()).invoke(p))).equals(getId.invoke(v));
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}

package spring.graphql.rest.nonoptimized.core.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.dto.ChildType;
import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.core.utility.GeneralUtility;
import spring.graphql.rest.nonoptimized.core.utility.GenericsUtility;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static spring.graphql.rest.nonoptimized.core.utility.GenericsUtility.*;

@Service
public class RQLMainProcessingUnit {

	public static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private final RQLProcessingUnitDistributor processingUnitDistributor;

	private final Logger logger = LoggerFactory.getLogger(RQLMainProcessingUnit.class);

	public RQLMainProcessingUnit(RQLProcessingUnitDistributor processingUnitDistributor) {
		this.processingUnitDistributor = processingUnitDistributor;
	}

	@Transactional(readOnly = true)
	public <T> void process(List<T> parents, PropertyNode node, List<PropertyNode> tree) throws NoSuchMethodException, IllegalAccessException {
		if (parents.size() == 0 || node.isCompleted()) {
			return;
		}
		Class<?> parentType = parents.get(0).getClass();
		ChildType childType = findChildTypeAndParentAccess(node, parentType);

		MethodHandle idGetter = findIdGetter(parentType);

		RQLProcessingUnit<?> processingUnit = processingUnitDistributor.findProcessingUnit(childType.getChildType());

		Set<String> ids = parents.stream().map(entity -> invokeHandle(String.class, idGetter, entity)).collect(Collectors.toSet());
		TransferResultDto<?> transferResult = processingUnit.process(tree, ids, node, childType.getParentAccessProperty());

		if (node.isOneToMany()) {
			oneToManyMapping(parents, transferResult, node, idGetter);
		} else if (node.isManyToMany()) {
			// TODO: Return many-to-many mappings
		}
	}

	private <T> void oneToManyMapping(List<T> parents, TransferResultDto<?> result, PropertyNode node, MethodHandle idGetter) throws NoSuchMethodException, IllegalAccessException {
		List<?> children = result.getResult();
		String parentProperty = result.getParent();

		if (children == null || children.size() == 0) {
			return;
		}

		Class<?> parentType = parents.get(0).getClass();
		MethodHandle childrenSetter = LOOKUP.findVirtual(parentType, "set" + GeneralUtility.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));

		HashMap<Class<?>, MethodHandle> parentHandlers = GenericsUtility.mapParentHandlers(parents, children, parentProperty);

		Map<Object, ? extends Set<?>> childMap = groupChildrenByParent(idGetter, children, parentType, parentHandlers);
		parents.forEach(parent -> mapChildrenToParent(idGetter, childMap, childrenSetter, parent));
	}

	private Map<Object, ? extends Set<?>> groupChildrenByParent(MethodHandle idGetter, List<?> children, Class<?> parentType, HashMap<Class<?>, MethodHandle> parentHandlers) {
		return children.stream().collect(groupingBy(child ->
						invokeHandle(String.class, idGetter, invokeHandle(parentType, parentHandlers.get(child.getClass()), child))
				, toSet()));
	}

	private <T> void mapChildrenToParent(MethodHandle idGetter, Map<?, ? extends Set<?>> childMap, MethodHandle childrenSetter, T parent) {
		try {
			Object parentId = invokeHandle(String.class, idGetter, parent);
			childrenSetter.invoke(parent, childMap.get(parentId));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

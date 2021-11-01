package spring.graphql.rest.rql.core.processing;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.core.dto.ChildType;
import spring.graphql.rest.rql.core.dto.TransferResultDto;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.core.utility.GeneralUtility;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static spring.graphql.rest.rql.core.utility.GenericsUtility.*;

@Service
public class RQLMainProcessingUnit {

	@Value("${rql.partition.elements.max-count}")
	private Integer maxPartitionCount;
	public static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private final RQLProcessingUnitDistributor processingUnitDistributor;
	private final Logger logger = LoggerFactory.getLogger(RQLMainProcessingUnit.class);

	public RQLMainProcessingUnit(RQLProcessingUnitDistributor processingUnitDistributor) {
		this.processingUnitDistributor = processingUnitDistributor;
	}

	public <T> void process(List<T> parents, PropertyNode node, List<PropertyNode> tree) throws NoSuchMethodException, IllegalAccessException {
		if (parents.size() == 0 || node.isCompleted()) {
			return;
		}
		Class<?> parentType = parents.get(0).getClass();
		ChildType childType = findChildTypeAndParentAccess(node, parentType);

		MethodHandle idGetter = findIdGetter(parentType);

		RQLProcessingUnit<?> processingUnit = processingUnitDistributor.findProcessingUnit(childType.getChildType());
		Set<String> ids = parents.stream().map(entity -> invokeHandle(String.class, idGetter, entity)).collect(toSet());
		TransferResultDto<?> transferResult = processingUnit.process(tree, ids, node, childType.getParentAccessProperty());
		try {
			if (node.isOneToMany()) {
				oneToManyMapping(parents, transferResult.getParent(), transferResult.getResult(), node, idGetter);
			} else if (node.isManyToMany()) {
				// TODO: Many-to-Many mappings
			}
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// TODO: Implement proper error-handling
			e.printStackTrace();
		}
	}

	private <T> void oneToManyMapping(List<T> parents, String parentProperty, List<?> children, PropertyNode node, MethodHandle idGetter) throws NoSuchMethodException, IllegalAccessException {
		if (children == null || children.size() == 0) {
			return;
		}

		Class<?> parentType = parents.get(0).getClass();
		MethodHandle childrenSetter = LOOKUP.findVirtual(parentType, "set" + GeneralUtility.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));
		MethodHandle childrenAdder = LOOKUP.findVirtual(Collection.class, "addAll", MethodType.methodType(boolean.class, Collection.class));
		MethodHandle childrenGetter = LOOKUP.findVirtual(parentType, "get" + GeneralUtility.capitalize(node.getProperty()), MethodType.methodType(Set.class));

		parents.forEach(parent -> {
			try {
				childrenSetter.invoke(parent, ConcurrentHashMap.newKeySet());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});

		HashMap<Class<?>, MethodHandle> parentHandlers = mapParentHandlers(parents, children, parentProperty);

		ArrayList<CompletableFuture<?>> futures = new ArrayList<>();
		Lists.partition(children, maxPartitionCount).forEach(_children ->
				futures.add(asyncMapping(parents, idGetter, parentType, childrenAdder, childrenGetter, parentHandlers, _children))
		);

		futures.forEach(CompletableFuture::join);
	}

	@NotNull
	private <T> CompletableFuture<Void> asyncMapping(List<T> parents, MethodHandle idGetter, Class<?> parentType, MethodHandle childrenAdder, MethodHandle childrenGetter, HashMap<Class<?>, MethodHandle> parentHandlers, List<?> _children) {
		return CompletableFuture.runAsync(() -> {
			Map<Object, ? extends Set<?>> childMap = groupByParent(idGetter, _children, parentType, parentHandlers);
			parents.forEach(parent -> addChildrenToParent(idGetter, childMap, childrenAdder, childrenGetter, parent));
		});
	}

	private Map<Object, ? extends Set<?>> groupByParent(MethodHandle idGetter, List<?> children, Class<?> parentType, HashMap<Class<?>, MethodHandle> parentHandlers) {
		return new ConcurrentHashMap<>(children.stream().collect(groupingBy(child ->
						invokeHandle(String.class, idGetter, invokeHandle(parentType, parentHandlers.get(child.getClass()), child))
				, toSet())));
	}

	private <T> void addChildrenToParent(MethodHandle idGetter, Map<?, ? extends Set<?>> childMap, MethodHandle childrenAdder, MethodHandle childrenGetter, T parent) {
		try {
			Object parentId = invokeHandle(String.class, idGetter, parent);
			Set<?> items = childMap.get(parentId);
			if (items == null || items.size() == 0) return;
			childrenAdder.invoke(childrenGetter.invoke(parent), items);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

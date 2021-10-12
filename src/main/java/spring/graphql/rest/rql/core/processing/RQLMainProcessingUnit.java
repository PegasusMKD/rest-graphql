package spring.graphql.rest.rql.core.processing;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static spring.graphql.rest.rql.core.utility.GenericsUtility.*;

@Service
public class RQLMainProcessingUnit {

	private final Executor taskExecutor;

	@Value("${rql.threads.count}")
	private Integer threadCount;

	@Value("${rql.partition.elements.max-count}")
	private Integer maxPartitionCount;

	public static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
	private final RQLProcessingUnitDistributor processingUnitDistributor;

	private final Logger logger = LoggerFactory.getLogger(RQLMainProcessingUnit.class);

	public RQLMainProcessingUnit(@Qualifier("taskExecutor") Executor taskExecutor, RQLProcessingUnitDistributor processingUnitDistributor) {
		this.taskExecutor = taskExecutor;
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
		Set<String> ids = parents.stream().map(entity -> invokeHandle(String.class, idGetter, entity)).collect(toSet());
		TransferResultDto<?> transferResult = processingUnit.process(tree, ids, node, childType.getParentAccessProperty());
		try {
			if (node.isOneToMany()) {
				oneToManyMapping(parents, transferResult.getParent(), transferResult.getResult(), node, idGetter);
			} else if (node.isManyToMany()) {
				// TODO: Return many-to-many mappings
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
		parents.forEach(parent -> {
			try {
				childrenSetter.invoke(parent, ConcurrentHashMap.newKeySet());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		MethodHandle childrenAdder = LOOKUP.findVirtual(Collection.class, "addAll", MethodType.methodType(boolean.class, Collection.class));
		MethodHandle childrenGetter = LOOKUP.findVirtual(parentType, "get" + GeneralUtility.capitalize(node.getProperty()), MethodType.methodType(Set.class));

		HashMap<Class<?>, MethodHandle> parentHandlers = mapParentHandlers(parents, children, parentProperty);

		List<? extends List<?>> childSegments = Lists.partition(children, maxPartitionCount);
		ArrayList<CompletableFuture<?>> futures = new ArrayList<>();
		for (List<?> _children : childSegments) {
			futures.add(CompletableFuture.runAsync(() -> {
				Map<Object, ? extends Set<?>> childMap = groupChildrenByParent(idGetter, _children, parentType, parentHandlers);
				parents.forEach(parent -> addChildrenToParent(idGetter, childMap, childrenAdder, childrenGetter, parent));
			}));
		}

		futures.forEach(CompletableFuture::join);
	}

	private Map<Object, ? extends Set<?>> groupChildrenByParent(MethodHandle idGetter, List<?> children, Class<?> parentType, HashMap<Class<?>, MethodHandle> parentHandlers) {
		return children.stream().collect(groupingBy(child ->
						invokeHandle(String.class, idGetter, invokeHandle(parentType, parentHandlers.get(child.getClass()), child))
				, toSet()));
	}

	private <T> void addChildrenToParent(MethodHandle idGetter, Map<?, ? extends Set<?>> childMap, MethodHandle childrenAdder, MethodHandle childrenGetter, T parent) {
		try {
			Object parentId = invokeHandle(String.class, idGetter, parent);
			childrenAdder.invoke(childrenGetter.invoke(parent), childMap.get(parentId));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

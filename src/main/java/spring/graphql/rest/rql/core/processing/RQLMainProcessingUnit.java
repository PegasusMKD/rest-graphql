package spring.graphql.rest.rql.core.processing;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static spring.graphql.rest.rql.core.utility.GenericsUtility.*;
import static spring.graphql.rest.rql.core.utility.GraphUtility.*;

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

		List<PropertyNode> subPartition = getSubPartition(tree, node);
		List<PropertyNode> currentPartition = getCurrentValidPartition(subPartition, node.getGraphPath())
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		subPartition.forEach(_node -> completeNode(node, currentPartition, _node));

		ArrayList<CompletableFuture<Void>> subFutures = new ArrayList<>();
		List<? extends List<?>> subSets = Lists.partition(parents, maxPartitionCount);
		for (List<?> set : subSets) {
			try {
				subFutures.add(parallelizedMapping(set, processingUnit, node, childType, idGetter,
						currentPartition, subPartition));
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}

		subFutures.forEach(CompletableFuture::join);
	}

	@Async("taskExecutor")
	@Transactional
	public <T> CompletableFuture<Void> parallelizedMapping(List<?> set, RQLProcessingUnit<?> processingUnit,
														   PropertyNode node, ChildType childType, MethodHandle idGetter,
														   List<PropertyNode> currentPartition, List<PropertyNode> subPartition) throws InstantiationException {
		try {
			Set<String> ids = set.stream().map(entity -> invokeHandle(String.class, idGetter, entity)).collect(toSet());
			TransferResultDto<?> transferResult = processingUnit.process(currentPartition, subPartition, ids, node, childType.getParentAccessProperty());

			if (node.isOneToMany()) {
				// TODO(Partitioning & Threading): Try to use "set" instead of "parents"
				oneToManyMapping(set, transferResult, node, idGetter);
			} else if (node.isManyToMany()) {
				// TODO: Return many-to-many mappings
			}
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// TODO: Implement proper error-handling
			e.printStackTrace();
		}

		return CompletableFuture.completedFuture(null);
	}

	private <T> void oneToManyMapping(List<T> parents, TransferResultDto<?> result, PropertyNode node, MethodHandle idGetter) throws NoSuchMethodException, IllegalAccessException {
		List<?> children = result.getResult();
		String parentProperty = result.getParent();

		if (children == null || children.size() == 0) {
			return;
		}

		Class<?> parentType = parents.get(0).getClass();
		MethodHandle childrenSetter = LOOKUP.findVirtual(parentType, "set" + GeneralUtility.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));

		HashMap<Class<?>, MethodHandle> parentHandlers = mapParentHandlers(parents, children, parentProperty);

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

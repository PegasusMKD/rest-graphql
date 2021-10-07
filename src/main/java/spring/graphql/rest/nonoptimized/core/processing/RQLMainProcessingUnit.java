package spring.graphql.rest.nonoptimized.core.processing;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.dto.ChildType;
import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;
import spring.graphql.rest.nonoptimized.core.utility.GeneralUtility;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		TransferResultDto<?> res = processingUnit.process(tree, ids, node, childType.getParentAccessProperty());

		if (node.isOneToMany()) {
			oneToManyMappingHandle(parents, res, node, idGetter);
		} else if (node.isManyToMany()) {
			// TODO: Return many-to-many mappings
		}
	}

	private <T> void oneToManyMappingHandle(List<T> parents, TransferResultDto<?> result, PropertyNode node, MethodHandle idGetter) throws NoSuchMethodException, IllegalAccessException {
		List<?> children = result.getResult();
		String parentProperty = result.getParent();

		if (children == null || children.size() == 0) {
			return;
		}

		MethodHandle childrenSetter = LOOKUP.findVirtual(parents.get(0).getClass(), "set" + GeneralUtility.capitalize(node.getProperty()), MethodType.methodType(void.class, Set.class));

		HashMap<Class<?>, MethodHandle> parentHandlers = findParentHandlers(parents, children, parentProperty);
		parents.forEach(parent -> mapChildrenToParentHandle(idGetter, children, childrenSetter, parentHandlers, parent));
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
	private <T> HashMap<Class<?>, MethodHandle> findParentHandlers(List<T> parents, List<?> children, String parentProperty) {
		HashMap<Class<?>, MethodHandle> parentHandlers = new HashMap<>();
		children.stream().map(Object::getClass).distinct().forEach(_clazz -> {
			try {
				parentHandlers.putIfAbsent(_clazz, LOOKUP.findVirtual(_clazz,
						"get" + GeneralUtility.capitalize(parentProperty),
						MethodType.methodType(parents.get(0).getClass())));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		return parentHandlers;
	}

	private <T> void mapChildrenToParentHandle(MethodHandle idGetter, List<?> children, MethodHandle childrenSetter, HashMap<Class<?>, MethodHandle> parentHandlers, T parent) {
		try {
			Set<?> appropriateChildren = children.stream().filter(child -> findChildrenByParentHandle(idGetter, parentHandlers, parent, child)).collect(Collectors.toSet());
			childrenSetter.invoke(parent, appropriateChildren);
			children.removeAll(appropriateChildren);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private <T> boolean findChildrenByParentHandle(MethodHandle idGetter, HashMap<Class<?>, MethodHandle> parentHandlers, T parent, Object child) {
		try {
			return (idGetter.invoke(parentHandlers.get(child.getClass()).invoke(child))).equals(idGetter.invoke(parent));
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}

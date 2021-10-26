package spring.graphql.rest.rql.core.internal;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.core.interfaces.QueryFunction;
import spring.graphql.rest.rql.core.interfaces.ValueExtractor;
import spring.graphql.rest.rql.core.nodes.PropertyNode;

import java.util.List;
import java.util.stream.Collectors;

import static spring.graphql.rest.rql.core.utility.GraphUtility.*;

@Service
public class RQLSyncInternal {

	private final RQLInternal rqlInternal;

	public RQLSyncInternal(RQLInternal rqlInternal) {
		this.rqlInternal = rqlInternal;
	}

	public <T, K> T rqlSelect(QueryFunction<T> queryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, String... attributePaths) {
		return rqlSelect(queryFunction, extractor, parentType, false, attributePaths);
	}

	public <T, K> T rqlSelect(QueryFunction<T> queryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, boolean isSingle, String... attributePaths) {
		List<PropertyNode> propertyNodes = createPropertyNodes(parentType, attributePaths);

		T queryResult = executeBaseQuery(queryFunction, propertyNodes, isSingle);
		List<K> queryData = extractor.extract(queryResult);

		rqlInternal.processSubPartitions(propertyNodes, queryData, "");

		return queryResult;
	}


	private <T> T executeBaseQuery(QueryFunction<T> queryFunction, List<PropertyNode> propertyNodes, boolean isSingle) {
		// TODO: Check if all requirements are met
		//  - Only one record
		//  - Only one OM relation for the parent (and all subsequent relations)
		//  then just do an eager call since it's more efficient
		//  ELSE, continue existing method
		List<PropertyNode> currentPartition = getCurrentValidPartition(propertyNodes, "")
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = currentPartition.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		if (isSingle && meetsEagerRequirements(propertyNodes, "")) {
			currentPartition = propertyNodes;
			paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		}

		EntityGraph graph = paths.isEmpty() ? EntityGraphs.empty() :
				EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0]));

		T queryResult = queryFunction.execute(graph);

		List<PropertyNode> finalCurrentPartition = currentPartition;
		propertyNodes.forEach(node -> completeNode(new PropertyNode(), finalCurrentPartition, node));
		return queryResult;
	}

	private boolean meetsEagerRequirements(List<PropertyNode> propertyNodes, String parentPropertyPath) {
		List<PropertyNode> partition = getCurrentValidPartition(propertyNodes, parentPropertyPath);
		if (partition.stream().filter(PropertyNode::isOneToMany).count() > 1) return false;
		if (partition.isEmpty()) return true;
		return partition.stream().allMatch(node -> meetsEagerRequirements(propertyNodes, node.getGraphPath()));
	}

}

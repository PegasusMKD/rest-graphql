package com.rest.graphql.rql.core.internal;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import com.rest.graphql.rql.core.utility.GraphUtility;
import org.springframework.stereotype.Service;
import com.rest.graphql.rql.core.interfaces.QueryFunction;
import com.rest.graphql.rql.core.interfaces.ValueExtractor;
import com.rest.graphql.rql.core.nodes.PropertyNode;

import java.util.List;
import java.util.stream.Collectors;

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
		List<PropertyNode> propertyNodes = GraphUtility.createPropertyNodes(parentType, attributePaths);

		T queryResult = executeBaseQuery(queryFunction, propertyNodes, isSingle);
		List<K> queryData = extractor.extract(queryResult);

		rqlInternal.processSubPartitions(propertyNodes, queryData, "");

		return queryResult;
	}


	private <T> T executeBaseQuery(QueryFunction<T> queryFunction, List<PropertyNode> propertyNodes, boolean isSingle) {
		List<PropertyNode> currentPartition = GraphUtility.getCurrentValidPartition(propertyNodes, "")
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
		propertyNodes.forEach(node -> GraphUtility.completeNode(new PropertyNode(), finalCurrentPartition, node));
		return queryResult;
	}

	private boolean meetsEagerRequirements(List<PropertyNode> propertyNodes, String parentPropertyPath) {
		List<PropertyNode> partition = GraphUtility.getCurrentValidPartition(propertyNodes, parentPropertyPath);
		if (partition.stream().filter(PropertyNode::isOneToMany).count() > 1) return false;
		if (partition.isEmpty()) return true;
		return partition.stream().allMatch(node -> meetsEagerRequirements(propertyNodes, node.getGraphPath()));
	}

}

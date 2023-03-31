package com.rql.core.internal;

import com.cosium.spring.data.jpa.entity.graph.domain2.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraphType;
import com.rql.core.utility.GraphUtility;
import com.rql.core.interfaces.QueryFunction;
import com.rql.core.interfaces.ValueExtractor;
import com.rql.core.nodes.PropertyNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RQLSyncInternal {

	private final RQLInternal rqlInternal;
	private final GraphUtility graphUtility;

	public <T, K> T rqlSelect(QueryFunction<T> queryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, String... attributePaths) {
		return rqlSelect(queryFunction, extractor, parentType, false, attributePaths);
	}

	public <T, K> T rqlSelect(QueryFunction<T> queryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, boolean isSingle, String... attributePaths) {
		List<PropertyNode> propertyNodes = graphUtility.createPropertyNodes(parentType, attributePaths);

		T queryResult = executeBaseQuery(queryFunction, propertyNodes, isSingle);
		List<K> queryData = extractor.extract(queryResult);

		rqlInternal.processSubPartitions(propertyNodes, queryData, "");

		return queryResult;
	}


	private <T> T executeBaseQuery(QueryFunction<T> queryFunction, List<PropertyNode> propertyNodes, boolean isSingle) {
		List<PropertyNode> currentPartition = graphUtility.getCurrentValidPartition(propertyNodes, "")
				.stream().filter(PropertyNode::isXToOne).collect(Collectors.toList());
		List<String> paths = currentPartition.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		if (isSingle && meetsEagerRequirements(propertyNodes, "")) {
			currentPartition = propertyNodes;
			paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		}

		DynamicEntityGraph.Builder dynamicGraph = DynamicEntityGraph.builder(EntityGraphType.LOAD);
		paths.forEach(dynamicGraph::addPath);
		EntityGraph graph = paths.isEmpty() ? DynamicEntityGraph.NOOP : dynamicGraph.build();

		T queryResult = queryFunction.execute(graph);

		List<PropertyNode> finalCurrentPartition = currentPartition;
		propertyNodes.forEach(node -> graphUtility.completeNode(new PropertyNode(), finalCurrentPartition, node));
		return queryResult;
	}

	private boolean meetsEagerRequirements(List<PropertyNode> propertyNodes, String parentPropertyPath) {
		List<PropertyNode> partition = graphUtility.getCurrentValidPartition(propertyNodes, parentPropertyPath);
		if (partition.stream().filter(PropertyNode::isOneToMany).count() > 1) return false;
		if (partition.isEmpty()) return true;
		return partition.stream().allMatch(node -> meetsEagerRequirements(propertyNodes, node.getGraphPath()));
	}

}

package com.rest.graphql.rql.core.utility;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;

import java.util.List;

public class EntityGraphUtility {

	public static EntityGraph getEagerEntityGraph(List<String> paths) {
		return paths.isEmpty() ? EntityGraphs.empty() :
				EntityGraphUtils.fromAttributePaths(EntityGraphType.FETCH, paths.toArray(new String[0]));
	}

}

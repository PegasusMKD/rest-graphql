package com.rql.core.utility;


import com.cosium.spring.data.jpa.entity.graph.domain2.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraphType;

import java.util.List;

public class EntityGraphUtility {

	public static EntityGraph getEagerEntityGraph(List<String> paths) {
		return paths.isEmpty() ? DynamicEntityGraph.NOOP :
				DynamicEntityGraph.builder(EntityGraphType.FETCH).addPath(paths.toArray(new String[0])).build();
	}

}

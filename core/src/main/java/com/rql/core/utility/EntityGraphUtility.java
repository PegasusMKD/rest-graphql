package com.rql.core.utility;


import com.cosium.spring.data.jpa.entity.graph.domain2.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraphType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityGraphUtility {

	public EntityGraph getEagerEntityGraph(List<String> paths) {
		return paths.isEmpty() ? DynamicEntityGraph.NOOP :
				DynamicEntityGraph.builder(EntityGraphType.FETCH).addPath(paths.toArray(new String[0])).build();
	}

}

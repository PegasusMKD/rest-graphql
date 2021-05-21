package spring.graphql.rest.nonoptimized.core.interfaces;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;

public interface InputFunction<T> {
	T accept(EntityGraph graph);
}
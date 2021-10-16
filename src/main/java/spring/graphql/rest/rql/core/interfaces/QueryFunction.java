package spring.graphql.rest.rql.core.interfaces;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;

@FunctionalInterface
public interface QueryFunction<T> {
	T execute(EntityGraph graph);
}

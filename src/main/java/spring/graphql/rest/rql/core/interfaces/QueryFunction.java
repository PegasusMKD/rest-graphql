package spring.graphql.rest.rql.core.interfaces;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;

/**
 * Interface for wrapping any query functions,
 * so that we can call the synchronous methods,
 * but with extra parameters if needed (like in async).
 *
 * @param <T> Expected response type
 */
@FunctionalInterface
public interface QueryFunction<T> {
	T execute(EntityGraph graph);
}

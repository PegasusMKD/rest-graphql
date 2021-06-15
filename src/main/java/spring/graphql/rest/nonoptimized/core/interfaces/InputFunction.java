package spring.graphql.rest.nonoptimized.core.interfaces;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;

/**
 * Interface which enables the user/developer to choose the starting query by using a lambda function,
 * method reference or class
 */
public interface InputFunction<T> {
	T accept(EntityGraph graph);
}
package spring.graphql.rest.rql.core.interfaces.query.functions;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.domain.Pageable;

// TODO(Documentation): Change document to reflect actual usage

/**
 * Interface which enables the user/developer to choose the starting query by using a lambda function,
 * method reference or class.
 *
 * @param <T> Type of result from query (ex. Page, Slice, List, etc.)
 */
@FunctionalInterface
public interface AsyncQueryFunction<T> {
	T execute(EntityGraph entityGraph, Pageable lazyLoadEvent);
}
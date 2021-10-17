package spring.graphql.rest.rql.core.interfaces.query.functions;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.springframework.data.domain.Pageable;

/**
 * Interface which enables the user/developer to choose the starting query by using a lambda function,
 * method reference or class. This is the asynchronous multi-threaded paginating equivalent of SyncQueryFunction.
 *
 * @param <T> Type of result from query (ex. Page, Slice, List, etc.)
 * @see spring.graphql.rest.rql.core.interfaces.query.functions.SyncQueryFunction
 */
@FunctionalInterface
public interface AsyncQueryFunction<T> {
	T execute(EntityGraph entityGraph, Pageable lazyLoadEvent);
}
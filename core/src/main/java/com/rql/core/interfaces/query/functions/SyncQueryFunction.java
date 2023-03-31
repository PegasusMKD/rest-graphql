package com.rql.core.interfaces.query.functions;


import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;

/**
 * Interface which enables the user/developer to choose the starting query by using a lambda function,
 * method reference or class.
 *
 * @param <T> Type of result from query (ex. Page, Slice, List, etc.)
 */
@FunctionalInterface
public interface SyncQueryFunction<T> {
	T execute(EntityGraph entityGraph);
}
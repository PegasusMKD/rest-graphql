package com.rest.graphql.rql.core.processing;

import com.rest.graphql.rql.core.dto.TransferResultDto;
import com.rest.graphql.rql.core.nodes.PropertyNode;

import java.util.List;
import java.util.Set;

/**
 * Wrapper class for the fetching of data through Spring Data JPA
 *
 * @param <T> Entity type (Account, Post, etc.)
 */
public interface RQLProcessingUnit<T> {

	TransferResultDto<T> process(List<PropertyNode> tree, Set<String> ids, PropertyNode node, String parentAccessProperty);

}

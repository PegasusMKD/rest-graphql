package com.rql.core.processing;

import com.rql.core.dto.TransferResultDto;
import com.rql.core.nodes.PropertyNode;

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

package spring.graphql.rest.nonoptimized.core.processing;

import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;

import java.util.List;
import java.util.Set;

/**
 * Wrapper class for the fetching of data through Spring Data JPA
 *
 * @param <T> Entity type (Account, Post, etc.)
 */
public interface RQLProcessingUnit<T> {

	TransferResultDto<T> process(List<PropertyNode> partition, Set<String> ids, PropertyNode node, String parentAccessProperty);

}

package spring.graphql.rest.nonoptimized.core.processing;

import spring.graphql.rest.nonoptimized.core.dto.TransferResultDto;
import spring.graphql.rest.nonoptimized.core.nodes.PropertyNode;

import java.util.List;
import java.util.Set;

public interface RQLProcessingUnit<T> {

	TransferResultDto<T> process(List<PropertyNode> tree, Set<String> data, PropertyNode node, String propertyToParent);

}

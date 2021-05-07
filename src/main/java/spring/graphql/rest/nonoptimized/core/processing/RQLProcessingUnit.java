package spring.graphql.rest.nonoptimized.core.processing;

import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.example.processors.dto.TransferResultDto;

import java.util.List;
import java.util.Set;

public interface RQLProcessingUnit<T> {

	TransferResultDto<T> process(List<PropertyNode> tree, Set<String> data, PropertyNode node, String propertyToParent);

}

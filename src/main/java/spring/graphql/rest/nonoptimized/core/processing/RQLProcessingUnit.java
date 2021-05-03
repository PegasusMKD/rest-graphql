package spring.graphql.rest.nonoptimized.core.processing;

import spring.graphql.rest.nonoptimized.core.PropertyNode;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface RQLProcessingUnit<T> {

	HashMap<String, Object> process(List<PropertyNode> tree, Set<String> data, PropertyNode node);

}

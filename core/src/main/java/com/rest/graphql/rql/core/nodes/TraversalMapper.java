package com.rest.graphql.rql.core.nodes;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TraversalMapper {

	@BeforeMapping
	default void resetPropertyNodes(Object entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes,
									@Context List<String> properties, @Context String property) {
		if (entity instanceof Collection || entity instanceof Map) {
			return;
		}

		currentPath.append(currentPath.length() != 0 ? "." : "").append(property);
		updateProperties(currentPath, propertyNodes, properties);
	}


	@AfterMapping
	default void cleanPath(Object entity, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes,
						   @Context List<String> properties) {
		if (entity instanceof Collection || entity instanceof Map) {
			return;
		}

		currentPath.delete(!currentPath.toString().contains(".") ? 0 : currentPath.lastIndexOf("."), currentPath.length());
		updateProperties(currentPath, propertyNodes, properties);
	}


	default void updateProperties(@Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties) {
		properties.removeIf(val -> !val.isEmpty());
		properties.addAll(propertyNodes.stream().filter(prop -> prop.getParentPropertyPath().equals(currentPath.toString()))
				.map(PropertyNode::getProperty).collect(Collectors.toList()));
	}
}

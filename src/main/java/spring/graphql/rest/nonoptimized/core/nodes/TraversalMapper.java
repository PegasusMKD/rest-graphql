package spring.graphql.rest.nonoptimized.core.nodes;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class TraversalMapper {

	@BeforeMapping
	public void resetGenericPropertyWrappers(Object vals, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes,
											 @Context List<String> properties, @Context String property) {
		if (vals instanceof Collection || vals instanceof Map) {
			return;
		}
		if (currentPath.length() == 0) {
			currentPath.append(property);
		} else {
			currentPath.append(".").append(property);
		}
		updateProperties(currentPath, propertyNodes, properties);
	}

	@AfterMapping
	public void cleanPath(Object vals, @Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes,
						  @Context List<String> properties) {
		if(vals instanceof Collection || vals instanceof Map) {
			return;
		}
		if(currentPath.toString().contains(".")) {
			currentPath.delete(currentPath.lastIndexOf("."), currentPath.length());
		} else {
			currentPath.delete(0, currentPath.length());
		}
		updateProperties(currentPath, propertyNodes, properties);
	}

	private void updateProperties(@Context StringBuilder currentPath, @Context List<PropertyNode> propertyNodes, @Context List<String> properties) {
		properties.removeIf(val -> !val.isEmpty());
		properties.addAll(propertyNodes.stream().filter(prop -> prop.getParentPropertyPath().equals(currentPath.toString()))
				.map(PropertyNode::getProperty).collect(Collectors.toList()));
	}
}

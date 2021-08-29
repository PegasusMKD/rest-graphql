package spring.graphql.rest.nonoptimized.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassAndPropertyDto {

	private Class<?> clazz;
	private String propertyToParent;

}

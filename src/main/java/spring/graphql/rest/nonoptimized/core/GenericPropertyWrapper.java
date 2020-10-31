package spring.graphql.rest.nonoptimized.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericPropertyWrapper {
	private String parentPropertyPath;
	private String property;
	private String graphPath;
}

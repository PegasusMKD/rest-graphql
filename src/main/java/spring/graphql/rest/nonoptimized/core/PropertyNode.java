package spring.graphql.rest.nonoptimized.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyNode {
	private String id = UUID.randomUUID().toString();

	private String parentPropertyPath;
	private String property;
	private String graphPath;

	private boolean oneToMany;
	private boolean manyToMany;

	private boolean completed = false;

	public PropertyNode(String parentPropertyPath, String property, String graphPath, boolean oneToMany) {
		id = UUID.randomUUID().toString();
		this.parentPropertyPath = parentPropertyPath;
		this.property = property;
		this.graphPath = graphPath;
		this.oneToMany = oneToMany;
		this.completed = false;

		// TODO: Implement with many-to-many support
		this.manyToMany = false;
	}
}

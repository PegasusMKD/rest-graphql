package com.rql.core.nodes;

import lombok.*;

import java.util.UUID;

@Data
@ToString
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
public class PropertyNode {
	private String id = UUID.randomUUID().toString();

	private String parentPropertyPath = "";
	private String property;
	private String graphPath;

	private boolean oneToMany;
	private boolean manyToMany;
	private boolean xToOne;

	private boolean completed = false;

	public PropertyNode(String parentPropertyPath, String property, String graphPath, boolean oneToMany, boolean xToOne) {
		id = UUID.randomUUID().toString();
		this.parentPropertyPath = parentPropertyPath;
		this.property = property;
		this.graphPath = graphPath;
		this.oneToMany = oneToMany;
		this.xToOne = xToOne;
		this.completed = false;

		// TODO: Implement with many-to-many support
		this.manyToMany = false;
	}
}

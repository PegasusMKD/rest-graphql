package com.rql.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChildType {

	private Class<?> childType;
	private String parentAccessProperty;

}

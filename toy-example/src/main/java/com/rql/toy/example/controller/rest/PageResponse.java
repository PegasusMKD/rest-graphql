package com.rql.toy.example.controller.rest;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

	private Integer totalPages;
	private Integer totalElements;
	private List<T> content;

	public PageResponse(int totalPages, long totalElements, List<T> content) {
		this(totalPages, new Long(totalElements).intValue(), content);
	}

	public PageResponse(int totalPages, int totalElements, List<T> content) {
		this.totalPages = totalPages;
		this.totalElements = totalElements;
		this.content = content;
	}
}

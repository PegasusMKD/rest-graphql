package com.rest.graphql.rql.core.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LazyLoadEvent {
	/**
	 * First row offset.
	 */
	private int first;

	/**
	 * Number of rows per page.
	 */
	private int rows;

	private String sortField;
	private String[] sortFields;
	private int sortOrder;

	public Pageable toPageable() {
		if (sortField != null) {
			return PageRequest.of(toPageIndex(), rows, toSortDirection(), sortField);
		} else if (sortFields != null) {
			return PageRequest.of(toPageIndex(), rows, toSortDirection(), sortFields);
		} else {
			return PageRequest.of(toPageIndex(), rows);
		}
	}

	/**
	 * Zero based page index.
	 */
	public int toPageIndex() {
		return rows > 0 ? (first + rows) / rows - 1 : 1;
	}

	public Sort.Direction toSortDirection() {
		return sortOrder == 1 ? Sort.Direction.ASC : Sort.Direction.DESC;
	}
}

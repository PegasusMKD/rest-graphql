package com.rql.core.pagination;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RQLPage implements Pageable {
	private int first;
	private int rows;
	private Sort sort;

	private String sortField = "id";
	private String[] sortFields;
	private Sort.Direction sortDirection = Sort.Direction.ASC;

	public RQLPage(int first, int rows, Sort sort) {
		if (first < 0) throw new IllegalArgumentException("Page offset must not be less than zero!");
		if (rows < 1) throw new IllegalArgumentException("Page size must not be less than one!");
		this.first = first;
		this.rows = rows;
		this.sort = sort;
	}

	public int getPageNumber() {
		return first / rows;
	}

	public int getPageSize() {
		return rows;
	}

	public long getOffset() {
		return first;
	}

	@NonNull
	@Override
	public Sort getSort() {
		if (sort != null)
			return sort;
		else if (sortField != null)
			return Sort.by(sortDirection, sortField);
		else if (sortFields != null)
			return Sort.by(sortDirection, sortFields);
		else
			return Sort.unsorted();
	}

	@NonNull
	public Pageable next() {
		return new RQLPage(first + rows, rows, sort);
	}

	@NonNull
	public Pageable previousOrFirst() {
		return new RQLPage(Math.max(first - rows, 0), rows, sort);
	}

	@NonNull
	public Pageable first() {
		return new RQLPage(0, rows, sort);
	}

	@Override
	public @NotNull Pageable withPage(int pageNumber) {
		return null;
	}

	public boolean hasPrevious() {
		return first > 0;
	}

}

package spring.graphql.rest.rql.core.pagination;

import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RQLPage implements Pageable {
	private int first;
	private int rows;
	private Sort sort;

	private String sortField;
	private String[] sortFields;
	private Sort.Direction sortDirection;

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
		if (sortField != null)
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

	public boolean hasPrevious() {
		return first > 0;
	}

}

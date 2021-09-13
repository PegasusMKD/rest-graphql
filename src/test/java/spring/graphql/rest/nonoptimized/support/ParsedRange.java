package spring.graphql.rest.nonoptimized.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ParsedRange {
	private int startColumnIndex;
	private int startRowIndex;
	private int endColumnIndex;
	private int endRowIndex;
}

package spring.graphql.rest.rql.support.spreadsheets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ParsedRange {
	private Integer startColumnIndex;
	private Integer startRowIndex;
	private Integer endColumnIndex;
	private Integer endRowIndex;
}

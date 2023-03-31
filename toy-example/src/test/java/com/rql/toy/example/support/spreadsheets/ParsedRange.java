package com.rql.toy.example.support.spreadsheets;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class ParsedRange {
	private Integer startColumnIndex;
	private Integer startRowIndex;
	private Integer endColumnIndex;
	private Integer endRowIndex;
}

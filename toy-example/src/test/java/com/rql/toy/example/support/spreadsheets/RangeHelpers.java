package com.rql.toy.example.support.spreadsheets;

public class RangeHelpers {

	public static String calculateRange(String sheetName, String startRow, String endColumn, int size) {
		String base = String.format("%s!%s:%s", sheetName, startRow, endColumn);
		int destinationRow = Integer.parseInt(startRow.substring(1)) + size;
		return base + destinationRow;
	}
}

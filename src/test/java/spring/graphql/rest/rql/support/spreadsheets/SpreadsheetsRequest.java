package spring.graphql.rest.rql.support.spreadsheets;

import com.google.api.services.sheets.v4.model.Request;

public interface SpreadsheetsRequest {
	Request execute();
}

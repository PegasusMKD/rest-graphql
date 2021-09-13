package spring.graphql.rest.nonoptimized;

import com.google.api.services.sheets.v4.model.Request;

public interface SpreadsheetsRequest {
	Request execute();
}

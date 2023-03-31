package com.rql.core.utility;

import org.springframework.stereotype.Component;

@Component
public class GeneralUtility {

	public String capitalize(String data) {
		return data.substring(0, 1).toUpperCase() + data.substring(1);
	}

}

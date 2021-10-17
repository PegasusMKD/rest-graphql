package spring.graphql.rest.rql.core.utility;

import java.util.ArrayList;
import java.util.List;

public class GeneralUtility {

	public static String capitalize(String data) {
		return data.substring(0, 1).toUpperCase() + data.substring(1);
	}

	public static <T> List<T> deepCopyList(List<T> items) {
		List<T> cpy = new ArrayList<>(items.size());
		cpy.addAll(items);
		return cpy;
	}
}

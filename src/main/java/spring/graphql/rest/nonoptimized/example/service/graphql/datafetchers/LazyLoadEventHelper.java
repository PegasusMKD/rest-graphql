package spring.graphql.rest.nonoptimized.example.service.graphql.datafetchers;

import graphql.schema.DataFetchingEnvironment;
import spring.graphql.rest.nonoptimized.core.rest.LazyLoadEvent;

public class LazyLoadEventHelper {

	public static LazyLoadEvent createLazyLoadEvent(DataFetchingEnvironment dataFetchingEnvironment) {
		LazyLoadEvent lazyLoadEvent = new LazyLoadEvent();
		lazyLoadEvent.setFirst(dataFetchingEnvironment.getArgument("first") != null ? dataFetchingEnvironment.getArgument("first") : 0);
		lazyLoadEvent.setRows(dataFetchingEnvironment.getArgument("rows") != null ? dataFetchingEnvironment.getArgument("rows") : Integer.MAX_VALUE);
		lazyLoadEvent.setSortField(dataFetchingEnvironment.getArgument("sortField") != null ? dataFetchingEnvironment.getArgument("sortField") : null);
		lazyLoadEvent.setSortFields(dataFetchingEnvironment.getArgument("sortFields") != null ? dataFetchingEnvironment.getArgument("sortFields") : null);
		lazyLoadEvent.setSortOrder(dataFetchingEnvironment.getArgument("sortOrder") != null ? dataFetchingEnvironment.getArgument("sortOrder") : 0);
		return lazyLoadEvent;
	}

}

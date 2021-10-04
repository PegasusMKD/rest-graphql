package spring.graphql.rest.nonoptimized.example.controller.support;

import spring.graphql.rest.nonoptimized.core.rest.LazyLoadEvent;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;

public class ControllerSupport {

	public static <T> void defaultLazyLoadEvent(PageRequestByExample<T> prbe) {
		LazyLoadEvent lazyLoadEvent = new LazyLoadEvent();
		if (prbe.getLazyLoadEvent() == null) {
			lazyLoadEvent.setFirst(0);
			lazyLoadEvent.setRows(Integer.MAX_VALUE);
			prbe.setLazyLoadEvent(lazyLoadEvent);
		}
	}

}

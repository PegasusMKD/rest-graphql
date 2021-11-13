package spring.graphql.rest.rql.example.controller.support;

import spring.graphql.rest.rql.example.controller.rest.LazyLoadEvent;
import spring.graphql.rest.rql.example.controller.rest.PageRequestByExample;

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

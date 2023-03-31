package com.rql.toy.example.controller.support;

import com.rql.core.dto.LazyLoadEvent;
import com.rql.toy.example.controller.rest.PageRequestByExample;

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

package spring.graphql.rest.nonoptimized.example.controller.support;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestParam;
import spring.graphql.rest.nonoptimized.core.rest.LazyLoadEvent;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;

public class ControllerSupport {

	@NotNull
	public static String[] defaultAttributePaths(@RequestParam(required = false) String[] attributePaths) {
		if (attributePaths == null) {
			attributePaths = new String[]{};
		}
		return attributePaths;
	}

	public static <T> void defaultLazyLoadEvent(PageRequestByExample<T> prbe) {
		LazyLoadEvent lazyLoadEvent = new LazyLoadEvent();
		if (prbe.getLazyLoadEvent() == null) {
			lazyLoadEvent.setFirst(0);
			lazyLoadEvent.setRows(Integer.MAX_VALUE);
			prbe.setLazyLoadEvent(lazyLoadEvent);
		}
	}

}

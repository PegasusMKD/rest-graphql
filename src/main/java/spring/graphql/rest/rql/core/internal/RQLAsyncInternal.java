package spring.graphql.rest.rql.core.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.core.interfaces.ValueExtractor;
import spring.graphql.rest.rql.core.interfaces.query.functions.AsyncQueryFunction;
import spring.graphql.rest.rql.example.controller.rest.LazyLoadEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RQLAsyncInternal {

	private final RQLSyncInternal rqlSyncInternal;
	@Value("${rql.threads.count}")
	private Integer threadCount;

	public RQLAsyncInternal(RQLSyncInternal rqlSyncInternal) {
		this.rqlSyncInternal = rqlSyncInternal;
	}

	public <T extends Page<K>, K> Page<K> asyncRQLSelectPagination(AsyncQueryFunction<T> asyncQueryFunction, ValueExtractor<T, K> extractor,
																   LazyLoadEvent lazyLoadEvent, Class<K> parentType, String... attributePaths) {
		List<LazyLoadEvent> events = partitionPagination(lazyLoadEvent);
		List<CompletableFuture<T>> fetches = events.stream()
				.map((event) -> CompletableFuture.supplyAsync(() ->
						rqlSyncInternal.rqlSelect((graph) -> asyncQueryFunction.execute(graph, event.toPageable()), extractor, parentType, event, attributePaths))
				).collect(Collectors.toList());
		return combinePages(fetches, lazyLoadEvent.toPageable());
	}

	<T extends Page<K>, K> Page<K> combinePages(List<CompletableFuture<T>> fetches, Pageable pageable) {
		CompletableFuture.allOf(fetches.toArray(new CompletableFuture[0])).join();
		List<T> pages = fetches.stream()
				.map(future -> future.getNow(null))
				.collect(Collectors.toList());
		List<K> data = pages.stream()
				.flatMap(page -> page.getContent().stream())
				.collect(Collectors.toList());
		return new PageImpl<>(data, pageable, pages.get(0).getTotalElements());
	}

	List<LazyLoadEvent> partitionPagination(LazyLoadEvent lazyLoadEvent) {
		int currentThreadCount = threadCount;
		int maxSize = lazyLoadEvent.getFirst() + lazyLoadEvent.getRows();
		int partitionSize = (int) Math.ceil((double) lazyLoadEvent.getRows() / currentThreadCount);

		if (currentThreadCount > lazyLoadEvent.getRows()) return Collections.singletonList(lazyLoadEvent);
		return IntStream.range(0, currentThreadCount + 1)
				.mapToObj(i -> LazyLoadEvent.builder()
						.rows(partitionSize)
						.first(Math.min(lazyLoadEvent.getFirst() + partitionSize * i, maxSize))
						.build())
				.filter(event -> event.getRows() > 0 && event.getFirst() < maxSize)
				.collect(Collectors.toList());
	}

}

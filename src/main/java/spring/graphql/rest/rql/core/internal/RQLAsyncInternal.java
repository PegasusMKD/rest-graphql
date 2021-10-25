package spring.graphql.rest.rql.core.internal;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.core.RQLAsyncRestriction;
import spring.graphql.rest.rql.core.interfaces.ValueExtractor;
import spring.graphql.rest.rql.core.interfaces.query.functions.AsyncQueryFunction;
import spring.graphql.rest.rql.core.pagination.RQLPage;
import spring.graphql.rest.rql.example.controller.rest.LazyLoadEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RQLAsyncInternal {

	private final RQLSyncInternal rqlSyncInternal;

	public RQLAsyncInternal(RQLSyncInternal rqlSyncInternal) {
		this.rqlSyncInternal = rqlSyncInternal;
	}

	public <T extends Page<K>, K> Page<K> asyncRQLSelectPagination(RQLAsyncRestriction restrictedBy, int amount, AsyncQueryFunction<T> asyncQueryFunction,
																   ValueExtractor<T, K> extractor, LazyLoadEvent lazyLoadEvent, Class<K> parentType,
																   String... attributePaths) {
		List<RQLPage> events = partitionPagination(restrictedBy, amount, lazyLoadEvent);
		List<CompletableFuture<T>> fetches = events.stream()
				.map((event) -> asyncRQLSelectWrap(asyncQueryFunction, extractor, parentType, event, attributePaths))
				.collect(Collectors.toList());
		return combinePages(fetches, lazyLoadEvent.toPageable());
	}

	@NotNull
	private <T extends Page<K>, K> CompletableFuture<T> asyncRQLSelectWrap(AsyncQueryFunction<T> asyncQueryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, RQLPage event, String[] attributePaths) {
		return CompletableFuture.supplyAsync(() ->
				rqlSyncInternal.rqlSelect((EntityGraph graph) ->
								asyncQueryFunction.execute(graph, event),
						extractor, parentType, attributePaths));
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

	List<RQLPage> partitionPagination(RQLAsyncRestriction restrictedBy, int amount, LazyLoadEvent lazyLoadEvent) {
		int threadCount;
		int partitionSize;

		int missingValue = (int) Math.ceil((double) lazyLoadEvent.getRows() / amount);

		if (restrictedBy == RQLAsyncRestriction.THREAD_COUNT) {
			threadCount = amount;
			partitionSize = missingValue;
		} else if (restrictedBy == RQLAsyncRestriction.PARTITION_SIZE) {
			threadCount = missingValue;
			partitionSize = amount;
		} else {
			throw new RuntimeException("No restriction has been set!");
		}

		int maxSize = lazyLoadEvent.getFirst() + lazyLoadEvent.getRows();

		if (threadCount > lazyLoadEvent.getRows())
			return lazyLoadEventToRQLPage(lazyLoadEvent);

		return IntStream.range(0, threadCount + 1)
				.mapToObj(partitionNumber -> createPartition(lazyLoadEvent, partitionSize, maxSize, partitionNumber))
				.filter(event -> event.getRows() > 0 && event.getFirst() < maxSize)
				.collect(Collectors.toList());
	}

	private RQLPage createPartition(LazyLoadEvent lazyLoadEvent, int partitionSize, int maxSize, int partitionNumber) {
		return RQLPage.builder()
				.rows(Math.min(maxSize - (lazyLoadEvent.getFirst() + partitionSize * partitionNumber), partitionSize))
				.first(Math.min(lazyLoadEvent.getFirst() + partitionSize * partitionNumber, maxSize))
				.sortField(lazyLoadEvent.getSortField())
				.sortFields(lazyLoadEvent.getSortFields())
				.sortDirection(lazyLoadEvent.toSortDirection())
				.build();
	}

	@NotNull
	private List<RQLPage> lazyLoadEventToRQLPage(LazyLoadEvent lazyLoadEvent) {
		return Collections.singletonList(RQLPage.builder()
				.first(lazyLoadEvent.getFirst())
				.rows(lazyLoadEvent.getRows())
				.sortField(lazyLoadEvent.getSortField())
				.sortFields(lazyLoadEvent.getSortFields())
				.sortDirection(lazyLoadEvent.toSortDirection())
				.build());
	}

}

package spring.graphql.rest.rql.core;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.core.interfaces.ValueExtractor;
import spring.graphql.rest.rql.core.interfaces.query.functions.AsyncQueryFunction;
import spring.graphql.rest.rql.core.interfaces.query.functions.SyncQueryFunction;
import spring.graphql.rest.rql.core.internal.RQLAsyncInternal;
import spring.graphql.rest.rql.core.internal.RQLSyncInternal;
import spring.graphql.rest.rql.example.controller.rest.LazyLoadEvent;

import java.util.Collections;
import java.util.List;

@Service
public class RQL {

	private final RQLSyncInternal rqlSyncInternal;

	private final RQLAsyncInternal rqlAsyncInternal;

	public RQL(RQLSyncInternal rqlSyncInternal, RQLAsyncInternal rqlAsyncInternal) {
		this.rqlSyncInternal = rqlSyncInternal;
		this.rqlAsyncInternal = rqlAsyncInternal;
	}

	public <K> K rqlSingleSelect(SyncQueryFunction<K> syncQueryFunction, Class<K> parentType, String... attributePaths) {
		return rqlSelect(syncQueryFunction, Collections::singletonList, parentType, true, attributePaths);
	}

	public <T extends List<K>, K> T rqlSelect(SyncQueryFunction<T> syncQueryFunction, Class<K> parentType, String... attributePaths) {
		return rqlSelect(syncQueryFunction, (T wrapper) -> wrapper, parentType, attributePaths);
	}

	public <T, K> T rqlSelect(SyncQueryFunction<T> syncQueryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, String... attributePaths) {
		return rqlSyncInternal.rqlSelect(syncQueryFunction::execute, extractor, parentType, false, attributePaths);
	}

	public <T, K> T rqlSelect(SyncQueryFunction<T> syncQueryFunction, ValueExtractor<T, K> extractor, Class<K> parentType, boolean isSingle, String... attributePaths) {
		return rqlSyncInternal.rqlSelect(syncQueryFunction::execute, extractor, parentType, isSingle, attributePaths);
	}

	public <T extends Page<K>, K> Page<K> asyncRQLSelectPagination(RQLAsyncRestriction restrictedBy, int amount, AsyncQueryFunction<T> asyncQueryFunction, ValueExtractor<T, K> extractor,
																   LazyLoadEvent lazyLoadEvent, Class<K> parentType, String... attributePaths) {
		return rqlAsyncInternal.asyncRQLSelectPagination(restrictedBy, amount, asyncQueryFunction, extractor, lazyLoadEvent, parentType, attributePaths);
	}
}

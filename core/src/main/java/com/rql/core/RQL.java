package com.rql.core;

import com.rql.core.dto.LazyLoadEvent;
import com.rql.core.interfaces.ValueExtractor;
import com.rql.core.interfaces.query.functions.AsyncQueryFunction;
import com.rql.core.internal.RQLAsyncInternal;
import com.rql.core.internal.RQLSyncInternal;
import com.rql.core.interfaces.query.functions.SyncQueryFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RQL {

	private final RQLSyncInternal rqlSyncInternal;

	private final RQLAsyncInternal rqlAsyncInternal;

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

	public <T extends Iterable<K>, K> T asyncRQLSelectPagination(RQLAsyncRestriction restrictedBy, int amount, AsyncQueryFunction<T> asyncQueryFunction,
																 ValueExtractor<T, K> extractor, LazyLoadEvent lazyLoadEvent, Class<K> parentType, String... attributePaths) {
		return (T) rqlAsyncInternal.asyncRQLSelectPagination(restrictedBy, amount, asyncQueryFunction, extractor, lazyLoadEvent, parentType, attributePaths);
	}
}

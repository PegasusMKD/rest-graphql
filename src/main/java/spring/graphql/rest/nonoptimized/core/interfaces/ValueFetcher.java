package spring.graphql.rest.nonoptimized.core.interfaces;

import java.util.List;

public interface ValueFetcher<T, K> {
	List<K> getValue(T val);
}

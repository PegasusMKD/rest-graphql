package com.rql.core.interfaces;

import java.util.List;

/**
 * Fetch/Extract the values/data inside a wrapper.
 * <br/>
 * For example:
 * <ul>
 *     <li><code>Page - ((Page page) -> page.getContent())</code></li>
 *     <li><code>CustomWrapper - ((CustomWrapper wrapper) -> page.getCustomGetter())</code></li>
 * </ul>
 * <p>
 * @param <K> Type of data
 * @param <T> Wrapper type
 */
@FunctionalInterface
public interface ValueExtractor<T, K> {
	List<K> extract(T wrapper);
}

package com.rql.toy.example.service.graphql.datafetchers;

import com.rest.graphql.rql.core.dto.LazyLoadEvent;
import com.rql.toy.example.models.Post;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.repository.PostRepository;

import java.util.Optional;

@Component
public class PostsFetcher implements DataFetcher<PageResponse<Post>> {
	private final PostRepository repository;

	public PostsFetcher(PostRepository repository) {
		this.repository = repository;
	}

	@Override
	public PageResponse<Post> get(DataFetchingEnvironment dataFetchingEnvironment) {
		LazyLoadEvent lazyLoadEvent = LazyLoadEventHelper.createLazyLoadEvent(dataFetchingEnvironment);
		Pageable pageable = Optional.of(lazyLoadEvent).map(LazyLoadEvent::toPageable).orElse(null);
		assert pageable != null;
		Page<Post> result = repository.findAll(pageable);
		return new PageResponse<>(result.getTotalPages(), result.getTotalElements(), result.getContent());
	}
}

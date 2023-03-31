package com.rql.toy.example.service.graphql.datafetchers;

import com.rql.core.dto.LazyLoadEvent;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.models.Comment;
import com.rql.toy.example.repository.CommentRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentsFetcher implements DataFetcher<PageResponse<Comment>> {

	private final CommentRepository repository;
	private final LazyLoadEventHelper lazyLoadEventHelper;

	@Override
	public PageResponse<Comment> get(DataFetchingEnvironment dataFetchingEnvironment) {
		LazyLoadEvent lazyLoadEvent = lazyLoadEventHelper.createLazyLoadEvent(dataFetchingEnvironment);
		Pageable pageable = Optional.of(lazyLoadEvent).map(LazyLoadEvent::toPageable).orElse(null);
		assert pageable != null;
		Page<Comment> result = repository.findAll(pageable);
		return new PageResponse<>(result.getTotalPages(), result.getTotalElements(), result.getContent());
	}

}

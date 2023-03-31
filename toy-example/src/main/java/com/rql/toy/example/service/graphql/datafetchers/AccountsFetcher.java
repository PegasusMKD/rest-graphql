package com.rql.toy.example.service.graphql.datafetchers;

import com.rest.graphql.rql.core.dto.LazyLoadEvent;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.models.Account;
import com.rql.toy.example.repository.AccountRepository;

import java.util.Optional;

@Component
public class AccountsFetcher implements DataFetcher<PageResponse<Account>> {

	private final AccountRepository repository;

	public AccountsFetcher(AccountRepository repository) {
		this.repository = repository;
	}

	@Override
	public PageResponse<Account> get(DataFetchingEnvironment dataFetchingEnvironment) {
		LazyLoadEvent lazyLoadEvent = LazyLoadEventHelper.createLazyLoadEvent(dataFetchingEnvironment);
		Pageable pageable = Optional.of(lazyLoadEvent).map(LazyLoadEvent::toPageable).orElse(null);
		assert pageable != null;
		Page<Account> result = repository.findAll(pageable);
		return new PageResponse<>(result.getTotalPages(), result.getTotalElements(), result.getContent());
	}
}

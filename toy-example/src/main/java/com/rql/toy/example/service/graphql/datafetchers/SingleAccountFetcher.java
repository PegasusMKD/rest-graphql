package com.rql.toy.example.service.graphql.datafetchers;

import com.rql.toy.example.models.Account;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;
import com.rql.toy.example.repository.AccountRepository;

@Component
public class SingleAccountFetcher implements DataFetcher<Account> {

	private final AccountRepository repository;

	public SingleAccountFetcher(AccountRepository repository) {
		this.repository = repository;
	}

	@Override
	public Account get(DataFetchingEnvironment dataFetchingEnvironment) {
		return repository.findById(dataFetchingEnvironment.getArgument("id"))
				.orElseThrow(() -> new RuntimeException("GraphQL: Should never be reached!!"));
	}
}

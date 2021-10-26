package spring.graphql.rest.rql.example.service.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.stereotype.Component;
import spring.graphql.rest.rql.example.models.Account;
import spring.graphql.rest.rql.example.repository.AccountRepository;

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

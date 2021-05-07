package spring.graphql.rest.nonoptimized.example.processors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.example.models.Account;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLAccountRepository;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("RQLAccount")
@Deprecated
public class RQLProcessingUnitAccount {

	private final RQLAccountRepository rqlAccountRepository;


	public RQLProcessingUnitAccount(RQLAccountRepository rqlAccountRepository) {
		this.rqlAccountRepository = rqlAccountRepository;
	}

	// TODO: Find smart way of implementing ManyToMany
	public List<Account> process(List<PropertyNode> node, List<Account> data) {
		// TODO: Just call generic fetch with method from this repo
		List<String> dataIds = data.stream().map(Account::getId).collect(Collectors.toList());
		List<Account> res = rqlAccountRepository.findAllByFriends_IdIn(dataIds);
		return data;
	}

}

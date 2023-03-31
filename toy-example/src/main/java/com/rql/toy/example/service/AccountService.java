package com.rql.toy.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.rest.graphql.rql.core.RQL;
import com.rest.graphql.rql.core.RQLAsyncRestriction;
import com.rest.graphql.rql.core.nodes.PropertyNode;
import com.rql.toy.example.controller.rest.PageRequestByExample;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.dto.AccountDto;
import com.rql.toy.example.dto.PersonDto;
import com.rql.toy.example.dto.querydsl.OptionalBooleanBuilder;
import com.rql.toy.example.mappers.RealAccountMapper;
import com.rql.toy.example.models.Account;
import com.rql.toy.example.models.QAccount;
import com.rql.toy.example.models.QPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rql.toy.example.repository.AccountRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.rest.graphql.rql.core.utility.GraphUtility.createPropertyNodes;


@Service
public class AccountService {

	private final Logger logger = LoggerFactory.getLogger(AccountService.class);

	private final AccountRepository accountRepository;
	private final RealAccountMapper accountMapper;
	private final RQL rql;

	public AccountService(AccountRepository accountRepository,
						  RealAccountMapper accountMapper, RQL rql) {
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
		this.rql = rql;
	}

	//	@RQLAOPRestrict(type = Account.class)
	public AccountDto findOne(String id, String[] attributePaths) {
		// Get minimal number of attributePaths for entity graph
		List<PropertyNode> propertyNodes = createPropertyNodes(Account.class, attributePaths);

		// Fetch data
		Account entity = rql.rqlSingleSelect(
				(EntityGraph graph) -> accountRepository.findById(id, graph)
						.orElseThrow(() -> new RuntimeException("Some exception")),
				Account.class, attributePaths
		);

		// Map properties
		return accountMapper.toAccountDto(entity, propertyNodes);
	}

	@Transactional
	public PageResponse<AccountDto> findAllAccounts(PageRequestByExample<AccountDto> prbe, String[] attributePaths) {
		AccountDto example = prbe.getExample() != null ? prbe.getExample() : new AccountDto();

		// Fetch data
		Page<Account> page = rql.asyncRQLSelectPagination(RQLAsyncRestriction.THREAD_COUNT, 5,
				(EntityGraph graph, Pageable pageable) -> accountRepository.findAll(makeFilter(example), pageable, graph),
				Slice::getContent, prbe.getLazyLoadEvent(), Account.class, attributePaths);

		// Get minimal number of attributePaths for entity graph
		List<PropertyNode> propertyNodes = createPropertyNodes(Account.class, attributePaths);

		// Map properties
		List<AccountDto> content = new ArrayList<>(accountMapper.toAccountDtos(page.getContent(), propertyNodes));

		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

	public PageResponse<AccountDto> findAllAccountsJPA(PageRequestByExample<AccountDto> prbe, String[] attributePaths) {
		AccountDto example = prbe.getExample() != null ? prbe.getExample() : new AccountDto();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = createPropertyNodes(Account.class, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Accounts", (endTime - startTime) / 1000000);

		// Fetch data
		Page<Account> page = accountRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<AccountDto> content =
				new ArrayList<>(accountMapper.toAccountDtos(new HashSet<>(page.getContent()), propertyNodes));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Accounts", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}


	private BooleanExpression makeFilter(AccountDto dto) {
		return makeFilter(dto, Optional.empty(), Optional.empty()).build();
	}

	public OptionalBooleanBuilder makeFilter(AccountDto dto, Optional<QAccount> _qAccount, Optional<OptionalBooleanBuilder> _opBuilder) {
		QAccount qAccount = _qAccount.orElse(QAccount.account);
		OptionalBooleanBuilder opBuilder = _opBuilder.orElse(OptionalBooleanBuilder.builder(qAccount.isNotNull()));
		if (dto == null) {
			return opBuilder;
		}

		opBuilder = makeFilter(dto.getPerson(), Optional.of(qAccount.person), Optional.of(opBuilder));

		return opBuilder.notNullAnd(qAccount.id::eq, dto.getId())
				.notEmptyAnd(qAccount.username::containsIgnoreCase, dto.getUsername());
	}

	public OptionalBooleanBuilder makeFilter(PersonDto dto, Optional<QPerson> _qPerson, Optional<OptionalBooleanBuilder> _opBuilder) {
		QPerson qPerson = _qPerson.orElse(QPerson.person);
		OptionalBooleanBuilder opBuilder = _opBuilder.orElse(OptionalBooleanBuilder.builder(qPerson.isNotNull()));
		if (dto == null) {
			return opBuilder;
		}

		opBuilder = makeFilter(dto.getAccount(), Optional.of(qPerson.account), Optional.of(opBuilder));

		return opBuilder.notNullAnd(qPerson.id::eq, dto.getId())
				.notEmptyAnd(qPerson.fullName::containsIgnoreCase, dto.getFullName())
				.notEmptyAnd(qPerson.phoneNumber::containsIgnoreCase, dto.getPhoneNumber());
	}

}

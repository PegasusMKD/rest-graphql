package spring.graphql.rest.rql.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.rql.core.RQL;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.core.utility.EntityGraphUtility;
import spring.graphql.rest.rql.example.controller.rest.LazyLoadEvent;
import spring.graphql.rest.rql.example.controller.rest.PageRequestByExample;
import spring.graphql.rest.rql.example.controller.rest.PageResponse;
import spring.graphql.rest.rql.example.dto.AccountDto;
import spring.graphql.rest.rql.example.dto.PersonDto;
import spring.graphql.rest.rql.example.dto.querydsl.OptionalBooleanBuilder;
import spring.graphql.rest.rql.example.mappers.AccountMapper;
import spring.graphql.rest.rql.example.models.Account;
import spring.graphql.rest.rql.example.models.QAccount;
import spring.graphql.rest.rql.example.models.QPerson;
import spring.graphql.rest.rql.example.repository.AccountRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static spring.graphql.rest.rql.core.utility.GraphUtility.createPropertyNodes;


@Service
public class AccountService {

	private final Logger logger = LoggerFactory.getLogger(AccountService.class);

	private final AccountRepository accountRepository;
	private final AccountMapper universalMapper;

	@Value("${rql.threads.count}")
	private Integer threadCount;

	private final RQL rql;


	public AccountService(AccountRepository accountRepository,
						  AccountMapper universalMapper, RQL rql) {
		this.accountRepository = accountRepository;
		this.universalMapper = universalMapper;
		this.rql = rql;
	}

	public AccountDto findOne(String id, String[] attributePaths) {
		// Get minimal number of attributePaths for entity graph
		List<PropertyNode> propertyNodes = createPropertyNodes(Account.class, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());

		// Fetch data
		Account entity = accountRepository.findById(id, EntityGraphUtility.getEagerEntityGraph(paths))
				.orElseThrow(() -> new RuntimeException("Some exception"));

		// Map properties
		List<String> props = new ArrayList<>();
		return universalMapper.toAccountDto(entity, new StringBuilder(), propertyNodes, props, "");
	}

	@Transactional
	public PageResponse<AccountDto> findAllAccounts(PageRequestByExample<AccountDto> prbe, String[] attributePaths) {
		AccountDto example = prbe.getExample() != null ? prbe.getExample() : new AccountDto();

		// Fetch data
		List<LazyLoadEvent> events = partitionLazyLoadEvents(prbe.getLazyLoadEvent());
		List<CompletableFuture<Page<Account>>> fetches = events.stream().map((event) -> CompletableFuture.supplyAsync(() -> rql.efficientCollectionFetch(
				(EntityGraph graph) -> accountRepository.findAll(makeFilter(example), event.toPageable(), graph),
				Slice::getContent, Account.class, attributePaths))).collect(Collectors.toList());
		Page<Account> page = combinePages(fetches);
		// Get minimal number of attributePaths for entity graph
		List<PropertyNode> propertyNodes = createPropertyNodes(Account.class, attributePaths);

		// Map properties
		long startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<AccountDto> content = new ArrayList<>(universalMapper.toAccountDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyNodes, props, ""));
		long endTime = System.nanoTime();
//		logger.info("Mapping of paths took: {} ms -- Accounts", (endTime - startTime) / 1000000);

		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

	private Page<Account> combinePages(List<CompletableFuture<Page<Account>>> fetches) {
		CompletableFuture.allOf(fetches.toArray(new CompletableFuture[0])).join();
		List<Page<Account>> pages = fetches.stream()
				.map(future -> future.getNow(null))
				.collect(Collectors.toList());
		List<Account> data = new ArrayList<>();
		for (Page<Account> page : pages) {
			data.addAll(page.getContent());
		}

		return new PageImpl<>(data, PageRequest.of(1, data.size()), pages.get(0).getTotalPages());
	}

	private List<LazyLoadEvent> partitionLazyLoadEvents(LazyLoadEvent lazyLoadEvent) {
		int partitionSize = (int) Math.ceil((double) lazyLoadEvent.getRows() / threadCount);

		return IntStream.range(1, threadCount + 1)
				.mapToObj(i -> LazyLoadEvent.builder().rows(partitionSize).first(lazyLoadEvent.getFirst() + (partitionSize * i) + 1).build())
				.collect(Collectors.toList());
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

package spring.graphql.rest.nonoptimized.example.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.querydsl.OptionalBooleanBuilder;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.core.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.dto.PersonDto;
import spring.graphql.rest.nonoptimized.example.mappers.AccountMapper;
import spring.graphql.rest.nonoptimized.example.models.*;
import spring.graphql.rest.nonoptimized.example.models.QPerson;
import spring.graphql.rest.nonoptimized.example.processors.RQLMainProcessingUnit;
import spring.graphql.rest.nonoptimized.example.repository.AccountRepository;
import spring.graphql.rest.nonoptimized.example.repository.PostRepository;

import java.util.*;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.getGenericPropertyWrappers;


@Service
public class AccountService {

	private Logger logger = LoggerFactory.getLogger(AccountService.class);

	private final RQLMainProcessingUnit rqlMainProcessingUnit;

	private final PostRepository postRepository;

	private final AccountRepository accountRepository;
	private final AccountMapper universalMapper;


	public AccountService(RQLMainProcessingUnit rqlMainProcessingUnit,
						  PostRepository postRepository, AccountRepository accountRepository,
						  AccountMapper universalMapper) {
		this.rqlMainProcessingUnit = rqlMainProcessingUnit;
		this.postRepository = postRepository;
		this.accountRepository = accountRepository;
		this.universalMapper = universalMapper;
	}

	public AccountDto findOne(String id, String[] attributePaths) {
		// Get minimal number of attributePaths for entity graph
		List<PropertyNode> propertyNodes = getGenericPropertyWrappers(Account.class, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());

		// Fetch data
		Account entity = accountRepository.findById(id, GraphHelpers.getEntityGraph(paths)).orElseThrow(() -> new RuntimeException("Some exception"));

		// Map properties
		List<String> props = new ArrayList<>();
		return universalMapper.toAccountDto(entity, new StringBuilder(), propertyNodes, props, "");
	}

	public PageResponse<AccountDto> findAllAccounts(PageRequestByExample<AccountDto> prbe, String[] attributePaths) throws NoSuchMethodException, IllegalAccessException {
		AccountDto example = prbe.getExample();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<PropertyNode> propertyNodes = getGenericPropertyWrappers(Account.class, attributePaths);
		List<String> paths = GraphHelpers.getPaths(propertyNodes);
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Accounts", (endTime - startTime) / 1000000);

		boolean containsRelation = false;
		if(paths.contains("posts")) {
			paths.removeIf(val -> val.contains("posts") || val.contains("comments"));
			containsRelation = true;
		}

		// Fetch data
		Page<Account> page = accountRepository.findAll(makeFilter(example), prbe.toPageable(), GraphHelpers.getEntityGraph(paths));

		for(PropertyNode node: propertyNodes) {
			if(paths.contains(node.getGraphPath())) {
				node.setCompleted(true);
			}
		}

		startTime = System.nanoTime();
		if(containsRelation) {
			rqlMainProcessingUnit.process(page.getContent(), propertyNodes.stream().filter(val -> val.getProperty().equals("posts")).findAny().get(), propertyNodes);
			rqlMainProcessingUnit.process(page.getContent(), propertyNodes.stream().filter(val -> val.getProperty().equals("comments")).findAny().get(), propertyNodes);
		}
		endTime = System.nanoTime();
		logger.info("Fetch posts: {} ms -- Posts", (endTime - startTime) / 1000000);

//		startTime = System.nanoTime();
//		if(containsRelation) {
//			List<Account> content = page.getContent();
//
//			LinkedHashMap<String, Account> dataMap = new LinkedHashMap<>();
//			content.forEach(val -> dataMap.put(val.getId(), val));
//			List<Post> posts = postRepository.findAllByPostedByIdIn(dataMap.keySet(), EntityGraphUtils.fromAttributePaths("postedBy"));
//			dataMap.forEach((k, v) -> v.setPosts(posts.stream().filter(p -> p.getPostedBy().getId().equals(k)).collect(Collectors.toSet())));
//			paths.add("posts");
//		}
//		endTime = System.nanoTime();
//		logger.info("Fetch posts: {} ms -- Posts", (endTime - startTime) / 1000000);


		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<AccountDto> content = new ArrayList<>(universalMapper.toAccountDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyNodes, props, ""));
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

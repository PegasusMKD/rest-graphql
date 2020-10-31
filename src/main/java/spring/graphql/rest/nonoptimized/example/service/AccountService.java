package spring.graphql.rest.nonoptimized.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.GenericMapperDecorator;
import spring.graphql.rest.nonoptimized.core.GenericPropertyWrapper;
import spring.graphql.rest.nonoptimized.core.UniversalMapper;
import spring.graphql.rest.nonoptimized.core.querydsl.OptionalBooleanBuilder;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.core.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.dto.CommentDto;
import spring.graphql.rest.nonoptimized.example.dto.PersonDto;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
import spring.graphql.rest.nonoptimized.example.models.*;
import spring.graphql.rest.nonoptimized.example.models.QComment;
import spring.graphql.rest.nonoptimized.example.models.QPerson;
import spring.graphql.rest.nonoptimized.example.models.QPost;
import spring.graphql.rest.nonoptimized.example.repository.AccountRepository;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AccountService {

	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

	private final AccountRepository accountRepository;
	private final UniversalMapper universalMapper;

	public AccountService(AccountRepository accountRepository,
						  UniversalMapper universalMapper) {
		this.accountRepository = accountRepository;
		this.universalMapper = universalMapper;
	}

	public AccountDto findOne(String id, String[] attributePaths) {
		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));
		List<GenericPropertyWrapper> propertyWrappers = paths.stream().map(val ->
				new GenericPropertyWrapper("", val, val)).collect(Collectors.toList());
		GenericMapperDecorator.getDefaultSubAttributes(Account.class, propertyWrappers, "", new ArrayList<>(), true);
		propertyWrappers = propertyWrappers.stream().distinct().collect(Collectors.toList());
		paths = propertyWrappers.stream().map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList());

		List<GenericPropertyWrapper> finalPropertyWrappers = propertyWrappers;
		List<String> props = new ArrayList<>();

		return universalMapper.toAccountDto(this.accountRepository.findById(id,
				paths.isEmpty() ? EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])))
				.orElseThrow(() -> new RuntimeException("Some exception")),
				new StringBuilder(), finalPropertyWrappers, props, "");
	}

	@Transactional
	public PageResponse<AccountDto> findAll(PageRequestByExample<AccountDto> prbe, String[] attributePaths) {
		AccountDto example = prbe.getExample();

		long startTime = System.nanoTime();

		List<String> paths = new ArrayList<>(Arrays.asList(attributePaths));
		List<GenericPropertyWrapper> propertyWrappers = paths.stream().map(val ->
				new GenericPropertyWrapper("", val, val)).collect(Collectors.toList());
		GenericMapperDecorator.getDefaultSubAttributes(Account.class, propertyWrappers, "", new ArrayList<>(), true);
		propertyWrappers = propertyWrappers.stream().distinct().collect(Collectors.toList());
		paths = propertyWrappers.stream().map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList());

		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms", (endTime - startTime) / 1000000);

		Page<Account> page = accountRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));

		startTime = System.nanoTime();

		List<GenericPropertyWrapper> finalPropertyWrappers = propertyWrappers;
		List<String> props = new ArrayList<>();

		List<AccountDto> content = new ArrayList<>(page.getContent()).stream()
				.map(account -> universalMapper.toAccountDto(account, new StringBuilder(),
						finalPropertyWrappers, props, "")).collect(Collectors.toList());

		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms", (endTime - startTime) / 1000000);

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

	// Making these other filters here since I'm too lazy to make seperate services

	public OptionalBooleanBuilder makeFilter(PostDto dto, Optional<QPost> _qPost, Optional<OptionalBooleanBuilder> _opBuilder) {
		QPost qPost = _qPost.orElse(QPost.post);
		OptionalBooleanBuilder opBuilder = _opBuilder.orElse(OptionalBooleanBuilder.builder(qPost.isNotNull()));
		if (dto == null) {
			return opBuilder;
		}

		opBuilder = makeFilter(dto.getPostedBy(), Optional.of(qPost.postedBy), Optional.of(opBuilder));

		return opBuilder.notNullAnd(qPost.id::eq, dto.getId())
						.notEmptyAnd(qPost.content::containsIgnoreCase, dto.getContent());
	}

	public OptionalBooleanBuilder makeFilter(CommentDto dto, Optional<QComment> _qComment, Optional<OptionalBooleanBuilder> _opBuilder) {
		QComment qComment = _qComment.orElse(QComment.comment);
		OptionalBooleanBuilder opBuilder = _opBuilder.orElse(OptionalBooleanBuilder.builder(qComment.isNotNull()));
		if (dto == null) {
			return opBuilder;
		}

		opBuilder = makeFilter(dto.getPost(), Optional.of(qComment.post), Optional.of(opBuilder));
		opBuilder = makeFilter(dto.getAccount(), Optional.of(qComment.account), Optional.of(opBuilder));
		
		return opBuilder.notNullAnd(qComment.id::eq, dto.getId())
						.notEmptyAnd(qComment.content::containsIgnoreCase, dto.getContent());
	}

	public OptionalBooleanBuilder makeFilter(PersonDto dto, Optional<QPerson> _qPerson, Optional<OptionalBooleanBuilder> _opBuilder) {
		QPerson qPerson = _qPerson.orElse (QPerson.person);
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

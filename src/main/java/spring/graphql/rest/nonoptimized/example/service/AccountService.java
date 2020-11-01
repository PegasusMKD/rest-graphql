package spring.graphql.rest.nonoptimized.example.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
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
import spring.graphql.rest.nonoptimized.example.repository.CommentRepository;
import spring.graphql.rest.nonoptimized.example.repository.PersonRepository;
import spring.graphql.rest.nonoptimized.example.repository.PostRepository;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.Helpers.getGenericPropertyWrappers;


@Service
public class AccountService {

	private Logger logger = LoggerFactory.getLogger(AccountService.class);

	private final AccountRepository accountRepository;
	private final UniversalMapper universalMapper;
	
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PersonRepository personRepository;

	public AccountService(AccountRepository accountRepository,
						  UniversalMapper universalMapper,
						  PostRepository postRepository,
						  CommentRepository commentRepository,
						  PersonRepository personRepository) {
		this.accountRepository = accountRepository;
		this.universalMapper = universalMapper;
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.personRepository = personRepository;
	}

	public AccountDto findOne(String id, String[] attributePaths) {
		// Get minimal number of attributePaths for entity graph
		List<GenericPropertyWrapper> propertyWrappers = getGenericPropertyWrappers(Account.class, attributePaths);
		List<String> paths = propertyWrappers.stream().map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList());

		// Fetch data
		Account entity = this.accountRepository.findById(id, paths.isEmpty() ? EntityGraphs.empty() : EntityGraphUtils
				.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0]))).orElseThrow(() -> new RuntimeException("Some exception"));

		// Map properties
		List<String> props = new ArrayList<>();
		return universalMapper.toAccountDto(entity, new StringBuilder(), propertyWrappers, props, "");
	}

	@Transactional
	public PageResponse<AccountDto> findAllAccounts(PageRequestByExample<AccountDto> prbe, String[] attributePaths) {
		AccountDto example = prbe.getExample();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<GenericPropertyWrapper> propertyWrappers = getGenericPropertyWrappers(Account.class, attributePaths);
		List<String> paths = propertyWrappers.stream().map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList());
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Accounts", (endTime - startTime) / 1000000);
		// Fetch data
		Page<Account> page = accountRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<AccountDto> content = new ArrayList<>(universalMapper.toAccountDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyWrappers, props, ""));
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

	// Making these other filters here since I'm too lazy to make seperate services

	private BooleanExpression makeFilter(PostDto dto) {
		return makeFilter(dto, Optional.empty(), Optional.empty()).build();
	}

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

	private BooleanExpression makeFilter(CommentDto dto) {
		return makeFilter(dto, Optional.empty(), Optional.empty()).build();
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

	public void populateDatabase() {
		Random random = new Random();

		Set<Person> people = new HashSet<>();
		for(int i=0;i<100;i++) {
			Person person = new Person();
			person.setFullName(String.format("name%d", i));
			person.setPhoneNumber(String.format("+%d", i));
			people.add(person);
		}
		people = new HashSet<>(personRepository.saveAll(people));
		logger.info("Added {} people", people.size());

		Set<Account> accounts = new HashSet<>();
		for(Person person: people) {
			Account account = new Account();
			account.setPerson(person);
			account.setUsername(String.format("user%d", accounts.size()+3));
			account.setFriends(accounts);
			accounts.add(account);
		}
		accounts = new HashSet<>(accountRepository.saveAll(accounts));
		logger.info("Added {} accounts", accounts.size());

		Set<Post> posts = new HashSet<>();
		for(Account account: accounts) {
			for(int i=0;i<100;i++) {
				Post post = new Post();
				post.setContent(String.format("content%d", (posts.size() + 1)*i));
				post.setPostedBy(account);
				posts.add(post);
			}
		}
		posts = new HashSet<>(postRepository.saveAll(posts));
		logger.info("Added {} posts", posts.size());

		List<Account> _accounts = new ArrayList<>(accounts);
		Set<Comment> comments = new HashSet<>();
		for(Post post: posts) {
			for(int i=0;i<100;i++) {
				Comment comment = new Comment();
				comment.setContent(String.format("content%d", (comments.size() + 1)*i));
				comment.setPost(post);
				comment.setAccount(_accounts.get(random.nextInt(_accounts.size())));
				comments.add(comment);
			}
		}

		commentRepository.saveAll(comments);
		logger.info("Added {} comments", comments.size());
		logger.info("Finished populating the database!");
	}

	@Transactional
	public PageResponse<PostDto> findAllPosts(PageRequestByExample<PostDto> prbe, String[] attributePaths) {
		PostDto example = prbe.getExample();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<GenericPropertyWrapper> propertyWrappers = getGenericPropertyWrappers(Post.class, attributePaths);
		List<String> paths = propertyWrappers.stream().map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList());
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);

		// Fetch data
		Page<Post> page = postRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<PostDto> content = new ArrayList<>(universalMapper.toPostDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyWrappers, props, ""));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Posts", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}

	@Transactional
	public PageResponse<CommentDto> findAllComments(PageRequestByExample<CommentDto> prbe, String[] attributePaths) {
		CommentDto example = prbe.getExample();

		// Get minimal number of attributePaths for entity graph
		long startTime = System.nanoTime();
		List<GenericPropertyWrapper> propertyWrappers = getGenericPropertyWrappers(Comment.class, attributePaths);
		List<String> paths = propertyWrappers.stream().map(GenericPropertyWrapper::getGraphPath).collect(Collectors.toList());
		long endTime = System.nanoTime();
		logger.info("Generation/traversal of paths took: {} ms -- Comments", (endTime - startTime) / 1000000);

		// Fetch data
		Page<Comment> page = commentRepository.findAll(makeFilter(example), prbe.toPageable(), paths.isEmpty() ?
				EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0])));

		// Map properties
		startTime = System.nanoTime();
		List<String> props = new ArrayList<>();
		List<CommentDto> content = new ArrayList<>(universalMapper.toCommentDtos(new HashSet<>(page.getContent()),
				new StringBuilder(), propertyWrappers, props, ""));
		endTime = System.nanoTime();
		logger.info("Mapping of paths took: {} ms -- Comments", (endTime - startTime) / 1000000);
		return new PageResponse<>(page.getTotalPages(), page.getTotalElements(), content);
	}
}

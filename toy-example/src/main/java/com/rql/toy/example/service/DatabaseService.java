package com.rql.toy.example.service;

import com.rql.toy.example.models.Account;
import com.rql.toy.example.models.Person;
import com.rql.toy.example.models.Post;
import com.rql.toy.example.repository.CommentRepository;
import com.rql.toy.example.repository.PersonRepository;
import com.rql.toy.example.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.rql.toy.example.models.Comment;
import com.rql.toy.example.repository.AccountRepository;

import java.util.*;

@Service
public class DatabaseService {

	private Logger logger = LoggerFactory.getLogger(DatabaseService.class);

	private final AccountRepository accountRepository;

	private final PostRepository postRepository;

	private final CommentRepository commentRepository;

	private final PersonRepository personRepository;

	private final Random random = new Random();

	public DatabaseService(AccountRepository accountRepository, PostRepository postRepository, CommentRepository commentRepository, PersonRepository personRepository) {
		this.accountRepository = accountRepository;
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.personRepository = personRepository;
	}

	public void populateDatabase() {
		Set<Person> people = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			Person person = new Person();
			person.setFullName(String.format("name%d", i));
			person.setPhoneNumber(String.format("+%d", i));
			people.add(person);
		}
		people = new HashSet<>(personRepository.saveAll(people));
		logger.info("Added {} people", people.size());

		Set<Account> accounts = new HashSet<>();
		for (Person person : people) {
			Account account = new Account();
			account.setPerson(person);
			account.setUsername(String.format("user%d", accounts.size() + 3));
			account.setFriends(accounts);
			accounts.add(account);
		}
		accounts = new HashSet<>(accountRepository.saveAll(accounts));
		logger.info("Added {} accounts", accounts.size());

		Set<Post> posts = new HashSet<>();
		for (Account account : accounts) {
			for (int i = 0; i < 100; i++) {
				Post post = new Post();
				post.setContent(String.format("content%d", (posts.size() + 1) * i));
				post.setPostedBy(account);
				posts.add(post);
			}
		}
		posts = new HashSet<>(postRepository.saveAll(posts));
		logger.info("Added {} posts", posts.size());

		List<Account> _accounts = new ArrayList<>(accounts);
		Set<Comment> comments = new HashSet<>();
		for (Post post : posts) {
			for (int i = 0; i < 60; i++) {
				Comment comment = new Comment();
				comment.setContent(String.format("content%d", (comments.size() + 1) * i));
				comment.setPost(post);
				comment.setAccount(_accounts.get(random.nextInt(_accounts.size())));
				comments.add(comment);
			}
		}

		commentRepository.saveAll(comments);
		logger.info("Added {} comments", comments.size());
		logger.info("Finished populating the database!");
	}


	public void createComments() {
		List<Account> _accounts = new ArrayList<>(accountRepository.findAll());
		int commentSize = 0;
		Set<Comment> comments = new HashSet<>();
		for (Post post : postRepository.findAll()) {
			for (int i = 0; i < 60; i++) {
				Comment comment = new Comment();
				comment.setContent(String.format("content%d", (comments.size() + 1) * i));
				comment.setPost(post);
				comment.setAccount(_accounts.get(random.nextInt(_accounts.size())));
				comments.add(comment);
				commentSize++;
			}
		}

		commentRepository.saveAll(comments);
		logger.info("Added {} comments", commentSize);
		logger.info("Finished populating the database!");
	}
}

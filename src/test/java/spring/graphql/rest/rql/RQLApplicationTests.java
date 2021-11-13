package spring.graphql.rest.rql;

import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.rql.example.controller.AccountController;
import spring.graphql.rest.rql.example.controller.AccountGraphQLController;
import spring.graphql.rest.rql.example.controller.rest.LazyLoadEvent;
import spring.graphql.rest.rql.example.controller.rest.PageRequestByExample;
import spring.graphql.rest.rql.example.dto.AccountDto;
import spring.graphql.rest.rql.support.GraphQLTests;
import spring.graphql.rest.rql.support.spreadsheets.RangeHelpers;
import spring.graphql.rest.rql.support.spreadsheets.SpreadsheetsAPI;
import spring.graphql.rest.rql.timer.TimedAction;
import spring.graphql.rest.rql.timer.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RQLApplicationTests {

	@Autowired
	private AccountController accountController;

	@Autowired
	private AccountGraphQLController graphQLController;

	@Autowired
	private SpreadsheetsAPI spreadsheetsAPI;

	private Logger logger = LoggerFactory.getLogger(RQLApplicationTests.class);

	@Test
	void contextLoads() {
		assertThat(accountController).isNotNull();
		assertThat(graphQLController).isNotNull();
	}

	// Tests
	List<List<Object>> basicAccounts() {
		final String type = "N#1";

		final PageRequestByExample<AccountDto> rqlExample = new PageRequestByExample<>();
		rqlExample.setExample(new AccountDto());

		final String graphQLExample = GraphQLTests.basicAccounts();
		return runTest(type, () -> accountController.findAllAccounts(rqlExample), graphQLExample, 20);
	}

	List<List<Object>> cartesianAccounts() {
		final String type = "COM#1";

		final PageRequestByExample<AccountDto> rqlExample = new PageRequestByExample<>();
		rqlExample.setExample(new AccountDto());
		rqlExample.setLazyLoadEvent(LazyLoadEvent.builder().first(0).rows(20).build());

		final String graphQLExample = GraphQLTests.cartesianAccounts();

		return runTest(type,
				() -> accountController.findAllAccounts(rqlExample, "comments", "posts"),
				graphQLExample, 6);
	}

	List<List<Object>> nestedAccounts() {
		final String type = "NOO#1";

		final PageRequestByExample<AccountDto> rqlExample = new PageRequestByExample<>();
		rqlExample.setExample(new AccountDto());

		final String graphQLExample = GraphQLTests.nestedAccounts();

		return runTest(type,
				() -> accountController.findAllAccounts(rqlExample, "person", "person.account",
						"person.account.person", "person.account.person.account"),
				graphQLExample, 20);
	}

	// Helpers with generics
	@NotNull
	private ArrayList<List<Object>> runTest(String type, TimedAction rqlAction, String graphQLExample, int iterations) {
		ArrayList<List<Object>> data = new ArrayList<>();

		// Skipping one so that the back-end gets "warmed up"
		for (int i = 0; i < iterations + 1; i++) {
//			logger.info("<---------- GQL -------->");
			String graphQLTime = Timer.roundedTimer(() -> graphQLController.executeGraphQLQuery(graphQLExample));

//			logger.info("<---------- RQL -------->");
			String rqlTime = Timer.roundedTimer(rqlAction);

			if (i == 0) continue;

			data.add(Lists.newArrayList(type, rqlTime, graphQLTime));
		}
		return data;
	}

	// Main
	@Test
	@Transactional
	void graphQLMetrics() {
		ArrayList<List<Object>> results = new ArrayList<>();
		results.addAll(basicAccounts());
		results.addAll(cartesianAccounts());
		results.addAll(nestedAccounts());
		// TODO(Benchmarks): Add nested posts one-to-many (comments, comments.post, comments.post.comments)
		pushResults(results);
		pushAverages(results);
	}


	void pushAverages(ArrayList<List<Object>> results) {
		List<String> types = results.stream().map(item -> item.get(0))
				.map(Object::toString).distinct().collect(Collectors.toList());
		ArrayList<List<Object>> averages = types.stream()
				.<List<Object>>map(type ->
						Lists.newArrayList(type, getRoundedAverageBenchmarks(results, type, 1),
								getRoundedAverageBenchmarks(results, type, 2))
				).collect(Collectors.toCollection(ArrayList::new));

		String clearRange = "Time!H5:J";

		spreadsheetsAPI.clearLatest(clearRange);
		spreadsheetsAPI.append(averages,
				RangeHelpers.calculateRange("Time", "H5", "J", averages.size()),
				true);
	}

	private String getRoundedAverageBenchmarks(ArrayList<List<Object>> results, String type, int idx) {
		return String.format("%.2f", results.stream()
				.filter(item -> item.get(0).equals(type))
				.map(item -> item.get(idx))
				.map(Object::toString)
				.mapToDouble(Double::parseDouble)
				.average().getAsDouble());
	}


	private void pushResults(ArrayList<List<Object>> results) {
		String clearRange = "Time!B5:D";

		spreadsheetsAPI.clearLatest(clearRange);
		spreadsheetsAPI.append(results,
				RangeHelpers.calculateRange("Time", "B5", "D", results.size()),
				true);
	}

}

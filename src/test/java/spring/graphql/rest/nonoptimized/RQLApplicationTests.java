package spring.graphql.rest.nonoptimized;

import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import spring.graphql.rest.nonoptimized.core.rest.LazyLoadEvent;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.example.controller.AccountController;
import spring.graphql.rest.nonoptimized.example.controller.AccountGraphQLController;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.support.GraphQLTests;
import spring.graphql.rest.nonoptimized.support.spreadsheets.RangeHelpers;
import spring.graphql.rest.nonoptimized.support.spreadsheets.SpreadsheetsAPI;
import spring.graphql.rest.nonoptimized.timer.TimedAction;
import spring.graphql.rest.nonoptimized.timer.Timer;

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

	@Test
	@Transactional
	void nestedAccountsTest() {
		System.out.println(nestedAccounts());
	}

	// Helpers with generics
	@NotNull
	private ArrayList<List<Object>> runTest(String type, TimedAction rqlAction, String graphQLExample, int iterations) {
		ArrayList<List<Object>> data = new ArrayList<>();

		// Skipping one so that the back-end gets "warmed up"
		for (int i = 0; i < iterations + 1; i++) {
			String rqlTime = Timer.roundedTimer(rqlAction);
			String graphQLTime = Timer.roundedTimer(() -> graphQLController.executeGraphQLQuery(graphQLExample));
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

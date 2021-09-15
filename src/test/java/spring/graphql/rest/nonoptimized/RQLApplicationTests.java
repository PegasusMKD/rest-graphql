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
import spring.graphql.rest.nonoptimized.support.RangeHelpers;
import spring.graphql.rest.nonoptimized.support.SpreadsheetsAPI;
import spring.graphql.rest.nonoptimized.timer.TimedAction;
import spring.graphql.rest.nonoptimized.timer.Timer;

import java.util.ArrayList;
import java.util.List;

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

	List<List<Object>> basicAccounts() {
		final String type = "N#1";

		final PageRequestByExample<AccountDto> rqlExample = new PageRequestByExample<>();
		rqlExample.setExample(new AccountDto());

		final String graphQLExample = "{\n" +
				"    allAccounts {\n" +
				"        totalPages\n" +
				"        totalElements\n" +
				"        content {\n" +
				"            id,\n" +
				"            username\n" +
				"        }\n" +
				"    }\n" +
				"}";

		return runTest(type, () -> accountController.findAllAccounts(rqlExample), graphQLExample, 20);
	}

	List<List<Object>> cartesianAccounts() {
		final String type = "COM#1";

		final PageRequestByExample<AccountDto> rqlExample = new PageRequestByExample<>();
		rqlExample.setExample(new AccountDto());
		rqlExample.setLazyLoadEvent(LazyLoadEvent.builder().first(0).rows(20).build());

		final String graphQLExample = "{\n" +
				"    allAccounts(first:0, rows:20) {\n" +
				"        totalPages\n" +
				"        totalElements\n" +
				"        content {\n" +
				"            id\n" +
				"            username\n" +
				"            comments {\n" +
				"                id\n" +
				"                content\n" +
				"                post {\n" +
				"                    id\n" +
				"                    content\n" +
				"                }\n" +
				"                account {\n" +
				"                    id\n" +
				"                    username\n" +
				"                }\n" +
				"            }\n" +
				"            posts {\n" +
				"                id\n" +
				"                content\n" +
				"                postedBy {\n" +
				"                    id\n" +
				"                    username\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"}";

		return runTest(type,
				() -> accountController.findAllAccounts(rqlExample, "comments", "posts"),
				graphQLExample, 6);
	}


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

	@Test
	@Transactional
	void graphQLMetrics() {
		ArrayList<List<Object>> results = new ArrayList<>();
		results.addAll(basicAccounts());
		results.addAll(cartesianAccounts());
		pushResults(results);
	}

	private void pushResults(ArrayList<List<Object>> results) {
		String clearRange = "Time!B5:D";

		spreadsheetsAPI.clearLatest(clearRange);
		spreadsheetsAPI.append(results,
				RangeHelpers.calculateRange("Time", "B5", "D", results.size()),
				true);
	}

}

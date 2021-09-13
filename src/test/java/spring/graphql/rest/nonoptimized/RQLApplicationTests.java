package spring.graphql.rest.nonoptimized;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.graphql.rest.nonoptimized.example.controller.AccountController;
import spring.graphql.rest.nonoptimized.example.controller.AccountGraphQLController;
import spring.graphql.rest.nonoptimized.support.SpreadsheetsAPI;

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

	@Test
	void graphQLMetrics() {
		spreadsheetsAPI.append(Lists.newArrayList(
				Lists.newArrayList("BOM#1", 10.5, 6),
				Lists.newArrayList("BOM#1", 10.5, 6)
		), "Time!B5:D6");
	}

}

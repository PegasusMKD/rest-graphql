package spring.graphql.rest.nonoptimized;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.graphql.rest.nonoptimized.example.controller.AccountController;
import spring.graphql.rest.nonoptimized.example.controller.AccountGraphQLController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NonOptimizedApplicationTests {

	@Autowired
	private AccountController accountController;
	@Autowired
	private AccountGraphQLController graphQLController;

	@Test
	void contextLoads() {
		assertThat(accountController).isNotNull();
		assertThat(graphQLController).isNotNull();
	}

	void graphQLMetrics() {

	}

}

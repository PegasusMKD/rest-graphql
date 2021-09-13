package spring.graphql.rest.nonoptimized.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.graphql.rest.nonoptimized.example.service.graphql.GraphQLService;

@RequestMapping("/users/graphql")
@RestController
public class AccountGraphQLController {

	private final GraphQLService graphQLService;

	public AccountGraphQLController(GraphQLService graphQLService) {
		this.graphQLService = graphQLService;
	}

	@PostMapping
	public ResponseEntity<Object> executeGraphQLQuery(@RequestBody String query) {
		return new ResponseEntity<>(graphQLService.graphQL().execute(query), HttpStatus.OK);
	}

}

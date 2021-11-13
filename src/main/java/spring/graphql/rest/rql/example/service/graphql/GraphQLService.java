package spring.graphql.rest.rql.example.service.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import spring.graphql.rest.rql.example.service.graphql.datafetchers.AccountsFetcher;
import spring.graphql.rest.rql.example.service.graphql.datafetchers.CommentsFetcher;
import spring.graphql.rest.rql.example.service.graphql.datafetchers.PostsFetcher;
import spring.graphql.rest.rql.example.service.graphql.datafetchers.SingleAccountFetcher;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Service
public class GraphQLService {

	private final AccountsFetcher accountsFetcher;
	private final SingleAccountFetcher singleAccountFetcher;
	private final PostsFetcher postsFetcher;
	private final CommentsFetcher commentsFetcher;
	@Value("classpath:graphql/account.graphql")
	Resource resource;
	private GraphQL graphQL;

	public GraphQLService(AccountsFetcher accountsFetcher, SingleAccountFetcher singleAccountFetcher, PostsFetcher postsFetcher, CommentsFetcher commentsFetcher) {
		this.accountsFetcher = accountsFetcher;
		this.singleAccountFetcher = singleAccountFetcher;
		this.postsFetcher = postsFetcher;
		this.commentsFetcher = commentsFetcher;
	}

	@PostConstruct
	private void loadSchemas() throws IOException {
		File schemaFile = resource.getFile();
		TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);
		RuntimeWiring wiring = buildWiring();
		GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry, wiring);
		graphQL = GraphQL.newGraphQL(schema).build();
	}

	private RuntimeWiring buildWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.type("Query", typeWiring -> typeWiring
						.dataFetcher("allAccounts", accountsFetcher)
						.dataFetcher("allPosts", postsFetcher)
						.dataFetcher("allComments", commentsFetcher)
						.dataFetcher("account", singleAccountFetcher)
				).build();
	}

	public GraphQL graphQL() {
		return graphQL;
	}
}

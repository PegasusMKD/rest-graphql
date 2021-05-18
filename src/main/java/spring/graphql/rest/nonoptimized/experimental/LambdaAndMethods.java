package spring.graphql.rest.nonoptimized.experimental;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.processors.RQLMainProcessingUnit;
import spring.graphql.rest.nonoptimized.example.repository.PostRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static spring.graphql.rest.nonoptimized.core.helpers.GraphHelpers.getGenericPropertyWrappers;

@Service
public class LambdaAndMethods {

	public interface InputFunction<T> {
		T accept(EntityGraph graph);
	}

	public interface ValueFetcher<T, K> {
		List<K> getValue(T val);
	}

	private final PostRepository postRepository;

	private final RQLMainProcessingUnit rqlMainProcessingUnit;

	public LambdaAndMethods(PostRepository postRepository, RQLMainProcessingUnit rqlMainProcessingUnit) {
		this.postRepository = postRepository;
		this.rqlMainProcessingUnit = rqlMainProcessingUnit;
	}

	public List<Post> experimentMultiple(String... attributePaths) throws NoSuchMethodException, IllegalAccessException {
		HashSet<String> data = new HashSet<>();
		return efficientCollectionFetch((EntityGraph graph) -> postRepository.findAllByPostedByIdIn(data, graph),
				(List<Post> val) -> val, Post.class, attributePaths);
	}


	public <T, K> T efficientCollectionFetch(InputFunction<T> o, ValueFetcher<T, K> fetcher, Class<K> clazz, String... attributePaths) throws NoSuchMethodException, IllegalAccessException {
		List<PropertyNode> propertyNodes = getGenericPropertyWrappers(clazz, attributePaths);
		List<String> paths = propertyNodes.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());

		boolean containsRelation = false;
		// TODO: Implement auto-removal from entity-graph tree
		if(paths.contains("comments")) {
			paths.removeIf(val -> val.contains("comments"));
			containsRelation = true;
		}
		EntityGraph graph = paths.isEmpty() ? EntityGraphs.empty() : EntityGraphUtils.fromAttributePaths(EntityGraphType.LOAD, paths.toArray(new String[0]));
		T startingData = o.accept(graph);
		List<K> array = fetcher.getValue(startingData);
		if(containsRelation) {
			rqlMainProcessingUnit.process(array, propertyNodes.stream().filter(val -> val.getProperty().equals("comments")).findAny().get(), propertyNodes);
		}

		return startingData;
	}

}

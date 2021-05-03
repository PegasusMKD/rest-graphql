package spring.graphql.rest.nonoptimized.example.processors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import spring.graphql.rest.nonoptimized.core.Helpers;
import spring.graphql.rest.nonoptimized.core.PropertyNode;
import spring.graphql.rest.nonoptimized.core.processing.RQLProcessingUnit;
import spring.graphql.rest.nonoptimized.example.models.Post;
import spring.graphql.rest.nonoptimized.example.processors.repository.RQLPostRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: Implement separation by property
@Service
@Qualifier("RQLPost")
public class RQLProcessingUnitPost implements RQLProcessingUnit<Post> {

	private final RQLPostRepository rqlPostRepository;


	public RQLProcessingUnitPost(RQLPostRepository rqlPostRepository) {
		this.rqlPostRepository = rqlPostRepository;
	}

	@Override
	public HashMap<String, Object> process(List<PropertyNode> tree, Set<String> data, PropertyNode node) {
		HashMap<String, Object> result = new HashMap<>();


		List<PropertyNode> subTree = tree.stream()
				.filter(val -> !val.isCompleted())
				.filter(val -> val.getParentPropertyPath().contains(node.getProperty()))
				.collect(Collectors.toList());

		List<PropertyNode> currentTree = subTree.stream().filter(val -> !val.isOneToMany()).collect(Collectors.toList());
		List<String> paths = Helpers.getPaths(currentTree).stream().filter(val -> val.equals(node.getGraphPath())).collect(Collectors.toList());
		paths.forEach(Helpers::lowerPath);

		List<Post> res = rqlPostRepository.findAllByPostedByIdIn(data, Helpers.getEntityGraph(paths));
		subTree.forEach(el -> {
			if(currentTree.contains(el) || el.getGraphPath().equals(node.getGraphPath())) {
				el.setCompleted(true);
			}
		});

		if(subTree.stream().anyMatch(val -> !val.isCompleted())) {
			// TODO: Implement generic function for fetch/processingUnit call
		}

		result.put("parent", "postedBy");
		result.put("data", res);

		return result;
	}
}
package spring.graphql.rest.nonoptimized.example.service.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import spring.graphql.rest.nonoptimized.example.controller.rest.LazyLoadEvent;
import spring.graphql.rest.nonoptimized.example.controller.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.models.Comment;
import spring.graphql.rest.nonoptimized.example.repository.CommentRepository;

import java.util.Optional;

@Component
public class CommentsFetcher implements DataFetcher<PageResponse<Comment>> {

	private final CommentRepository repository;

	public CommentsFetcher(CommentRepository repository) {
		this.repository = repository;
	}

	@Override
	public PageResponse<Comment> get(DataFetchingEnvironment dataFetchingEnvironment) {
		LazyLoadEvent lazyLoadEvent = LazyLoadEventHelper.createLazyLoadEvent(dataFetchingEnvironment);
		Pageable pageable = Optional.of(lazyLoadEvent).map(LazyLoadEvent::toPageable).orElse(null);
		assert pageable != null;
		Page<Comment> result = repository.findAll(pageable);
		return new PageResponse<>(result.getTotalPages(), result.getTotalElements(), result.getContent());
	}

}

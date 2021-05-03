package spring.graphql.rest.nonoptimized.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import spring.graphql.rest.nonoptimized.example.models.Post;

import java.util.List;
import java.util.Set;

public interface RQLPostRepository extends EntityGraphJpaRepository<Post, String>, QuerydslPredicateExecutor<Post> {

	List<Post> findAllByPostedByIdIn(Set<String> ids, EntityGraph entityGraph);
}

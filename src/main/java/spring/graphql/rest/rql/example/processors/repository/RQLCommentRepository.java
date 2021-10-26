package spring.graphql.rest.rql.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import spring.graphql.rest.rql.example.models.Comment;

import java.util.List;
import java.util.Set;

public interface RQLCommentRepository extends EntityGraphJpaRepository<Comment, String>, QuerydslPredicateExecutor<Comment> {

	List<Comment> findAllByPostIdIn(Set<String> ids, Pageable pageable, EntityGraph graph);

	List<Comment> findAllByAccountIdIn(Set<String> ids, Pageable pageable, EntityGraph graph);

	int countAllByPostIdIn(Set<String> ids);

	int countAllByAccountIdIn(Set<String> ids);

}

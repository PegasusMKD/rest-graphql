package spring.graphql.rest.nonoptimized.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import spring.graphql.rest.nonoptimized.example.models.Comment;
import spring.graphql.rest.nonoptimized.example.models.Post;

import java.util.List;
import java.util.Set;

public interface RQLCommentRepository extends EntityGraphJpaRepository<Comment, String>, QuerydslPredicateExecutor<Comment> {

	List<Comment> findAllByPostIdIn(Set<String> ids, EntityGraph graph);

}

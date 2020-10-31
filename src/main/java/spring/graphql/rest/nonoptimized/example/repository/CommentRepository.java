package spring.graphql.rest.nonoptimized.example.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;
import spring.graphql.rest.nonoptimized.example.models.Comment;

public interface CommentRepository extends EntityGraphJpaRepository<Comment, String>, QuerydslPredicateExecutor<Comment> {

	@NonNull
	Page<Comment> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable, EntityGraph graph);
}

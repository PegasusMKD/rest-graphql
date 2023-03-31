package com.rql.toy.example.repository;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.querydsl.core.types.Predicate;
import com.rql.core.repositories.RQLRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;
import com.rql.toy.example.models.Comment;

public interface CommentRepository extends RQLRepository<Comment, String> {

	@NonNull
	Page<Comment> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable, EntityGraph graph);
}

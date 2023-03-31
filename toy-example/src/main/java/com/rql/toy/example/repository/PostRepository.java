package com.rql.toy.example.repository;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.querydsl.core.types.Predicate;
import com.rql.toy.example.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;

public interface PostRepository extends EntityGraphJpaRepository<Post, String>, QuerydslPredicateExecutor<Post> {

	@NonNull
	Page<Post> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable, EntityGraph graph);
}

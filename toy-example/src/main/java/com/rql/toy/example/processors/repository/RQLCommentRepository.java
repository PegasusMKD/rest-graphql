package com.rql.toy.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.rql.toy.example.models.Comment;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Set;

public interface RQLCommentRepository extends EntityGraphJpaRepository<Comment, String>, QuerydslPredicateExecutor<Comment> {

	List<Comment> findAllByPostIdIn(Set<String> ids, EntityGraph graph);

	List<Comment> findAllByAccountIdIn(Set<String> ids, EntityGraph graph);

	int countAllByPostIdIn(Set<String> ids);

	int countAllByAccountIdIn(Set<String> ids);

}

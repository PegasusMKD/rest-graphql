package com.rql.toy.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.rql.toy.example.models.Post;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Set;

public interface RQLPostRepository extends EntityGraphJpaRepository<Post, String>, QuerydslPredicateExecutor<Post> {

	List<Post> findAllByPostedByIdIn(Set<String> ids, EntityGraph entityGraph);

	int countAllByPostedByIdIn(Set<String> ids);
}

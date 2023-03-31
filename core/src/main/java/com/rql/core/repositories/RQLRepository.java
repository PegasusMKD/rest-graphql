package com.rql.core.repositories;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface RQLRepository<T, ID> extends EntityGraphJpaRepository<T, ID>, QuerydslPredicateExecutor<T> {
}

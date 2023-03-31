package com.rql.toy.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.rql.toy.example.models.Account;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface RQLAccountRepository extends EntityGraphJpaRepository<Account, String>, QuerydslPredicateExecutor<Account> {

}

package spring.graphql.rest.nonoptimized.example.processors.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import spring.graphql.rest.nonoptimized.example.models.Account;

public interface RQLAccountRepository extends EntityGraphJpaRepository<Account, String>, QuerydslPredicateExecutor<Account> {

}

package com.rql.tpcc.repositories;

import com.rql.tpcc.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}

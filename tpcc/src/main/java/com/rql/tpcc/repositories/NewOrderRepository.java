package com.rql.tpcc.repositories;

import com.rql.tpcc.models.Customer;
import com.rql.tpcc.models.NewOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewOrderRepository extends JpaRepository<NewOrder, String> {
}

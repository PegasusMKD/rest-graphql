package com.rql.tpcc.repositories;

import com.rql.tpcc.models.Customer;
import com.rql.tpcc.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, String> {
}

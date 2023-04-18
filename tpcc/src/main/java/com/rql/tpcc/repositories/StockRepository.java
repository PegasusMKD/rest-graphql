package com.rql.tpcc.repositories;

import com.rql.tpcc.models.Customer;
import com.rql.tpcc.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {
}

package com.rql.tpcc.repositories;

import com.rql.tpcc.models.Customer;
import com.rql.tpcc.models.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, String> {
}

package com.rql.tpcc.repositories;

import com.rql.tpcc.models.Customer;
import com.rql.tpcc.models.District;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, String> {
}

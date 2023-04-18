package com.rql.tpcc.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouse")
public class Warehouse {

	@Id
	@Column(name = "w_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@Column(name = "w_name", length = 10)
	private String name;
	@Column(name = "w_street_1", length = 20)
	private String street1;
	@Column(name = "w_street_2", length = 20)
	private String street2;
	@Column(name = "w_city", length = 20)
	private String city;
	@Column(name = "w_state", length = 2)
	private String state;
	@Column(name = "w_zip", length = 9)
	private String zip;
	@Column(name = "w_tax")
	private Double tax;
	@Column(name = "w_ytd")
	private Double yearToDate;
}

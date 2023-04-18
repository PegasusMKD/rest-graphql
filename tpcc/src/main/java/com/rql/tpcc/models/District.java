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
@Table(name = "district")
public class District {
	@Id
	@Column(name = "d_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Warehouse warehouse;

	@Column(name = "d_name", length = 10)
	private String name;
	@Column(name = "d_street_1", length = 20)
	private String street_1;
	@Column(name = "d_street_2", length = 20)
	private String street_2;
	@Column(name = "d_city", length = 20)
	private String city;
	@Column(name = "d_state", length = 2)
	private String state;
	@Column(name = "d_zip", length = 9)
	private String zip;
	@Column(name = "d_tax")
	private Double tax;
	@Column(name = "d_ytd")
	private Double ytd;

	// TODO: Check if this is an "auto-generated" number, as in, "this is the next available order number in this district"
	@Column(name = "d_next_o_id")
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String nextOrder;

}

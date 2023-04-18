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
@Table(name = "new_order")
public class NewOrder {

	@Id
	@Column(name = "no_o_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;

	@OneToOne(fetch = FetchType.LAZY)
	private Order order;
}

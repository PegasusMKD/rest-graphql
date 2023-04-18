package com.rql.tpcc.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order")
public class Order {

	@Id
	@Column(name = "o_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	private Customer customer;
	@Column(name = "o_entry_d")
	private LocalDateTime entryDateTime;
	@Column(name = "o_carrier_id")
	private String o_carrier_id;
	@Column(name = "o_ol_cnt")
	private Integer orderLineCount;
	@Column(name = "o_all_local", columnDefinition = "numeric(1)")
	private boolean local;

}

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
@Table(name = "history")
public class History {

	@Id
	@Column(name = "h_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	private Customer customer;
	@ManyToOne(fetch = FetchType.LAZY)
	private District district;
	@Column(name = "h_date")
	private LocalDateTime date;
	@Column(name = "h_amount")
	private Double amount;
	@Column(name = "h_data", length = 24)
	private String data;
}

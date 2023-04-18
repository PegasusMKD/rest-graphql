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
@Table(name = "order_line")
public class OrderLine {

	@Id
	@Column(name = "ol_o_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@Column(name = "ol_number")
	private String number;
	@ManyToOne(fetch = FetchType.LAZY)
	private Order order;
	@ManyToOne(fetch = FetchType.LAZY)
	private Stock stock;
	@Column(name = "ol_delivery_d")
	private LocalDateTime deliveryDate;
	@Column(name = "ol_quantity")
	private Integer quantity;
	@Column(name = "ol_amount")
	private Double amount;
	@Column(name = "ol_dist_info", length = 24)
	private String districtInfo;

}

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
@Table(name = "stock")
public class Stock {

	@Id
	@Column(name = "s_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	private Item item;
	@ManyToOne(fetch = FetchType.LAZY)
	private Warehouse warehouse;
	@Column(name = "s_quantity")
	private Integer quantity;
	@Column(name = "s_dist_01", length = 24)
	private String dist01;
	@Column(name = "s_dist_02", length = 24)
	private String dist02;
	@Column(name = "s_dist_03", length = 24)
	private String dist03;
	@Column(name = "s_dist_04", length = 24)
	private String dist04;
	@Column(name = "s_dist_05", length = 24)
	private String dist05;
	@Column(name = "s_dist_06", length = 24)
	private String dist06;
	@Column(name = "s_dist_07", length = 24)
	private String dist07;
	@Column(name = "s_dist_08", length = 24)
	private String dist08;
	@Column(name = "s_dist_09", length = 24)
	private String dist09;
	@Column(name = "s_dist_10", length = 24)
	private String dist10;
	@Column(name = "s_ytd")
	private Integer ytd;
	@Column(name = "s_order_cnt")
	private Integer orderCount;
	@Column(name = "s_remote_cnt")
	private Integer remoteCount;
	@Column(name = "s_data", length = 50)
	private String data;
}

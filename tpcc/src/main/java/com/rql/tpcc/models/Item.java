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
@Table(name = "item")
public class Item {
	@Id
	@Column(name = "i_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@Column(name = "i_im_id")
	private String imageId;
	@Column(name = "i_name", length = 24)
	private String name;
	@Column(name = "i_price")
	private Double price;
	@Column(name = "i_data", length = 50)
	private String data;

}

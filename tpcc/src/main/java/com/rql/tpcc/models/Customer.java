package com.rql.tpcc.models;

import com.rql.tpcc.models.enums.CreditType;
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
@Table(name = "customer")
public class Customer {
	@Id
	@Column(name = "c_id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	private District district;
	@Column(name = "c_first", length = 16)
	private String firstName;
	@Column(name = "c_middle", length = 2)
	private String middleName;
	@Column(name = "c_last", length = 16)
	private String lastName;
	@Column(name = "c_street_1", length = 20)
	private String street1;
	@Column(name = "c_street_2", length = 20)
	private String street2;
	@Column(name = "c_city", length = 20)
	private String city;
	@Column(name = "c_state", length = 2)
	private String state;
	@Column(name = "c_zip", length = 9)
	private String zip;
	@Column(name = "c_phone", length = 16)
	private String phone;
	@Column(name = "c_since")
	private LocalDateTime since;
	@Column(name = "c_credit")
	@Enumerated(EnumType.STRING)
	private CreditType creditType;
	@Column(name = "c_credit_lim")
	private Double creditLimit;
	@Column(name = "c_discount")
	private Double discount;
	@Column(name = "c_balance")
	private Double balance;
	@Column(name = "c_ytd_payment")
	private Double yearToDatePayment;
	@Column(name = "c_payment_cnt")
	private Integer paymentCount;
	@Column(name = "c_delivery_cnt")
	private Integer deliveryCount;
	@Column(name = "c_data", length = 500)
	private String data;

}

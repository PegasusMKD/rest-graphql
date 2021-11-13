package spring.graphql.rest.rql.example.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity(name = "Person")
@Table(name = "person")
@EqualsAndHashCode(exclude = "account")
@ToString(exclude = "account")
public class Person {

	@Column(name = "id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	@Id
	private String id;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "phone_number")
	private String phoneNumber;

	@OneToOne(mappedBy = "person", fetch = FetchType.LAZY, optional = false)
	private Account account;
}

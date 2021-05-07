package spring.graphql.rest.nonoptimized.example.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity(name = "Account")
@Table(name = "account")
@EqualsAndHashCode(exclude = {"posts", "friends", "person"})
@ToString(exclude = {"posts", "friends", "person"})
public class Account {

	@Column(name = "id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	@Id
	private String id;

	@Column(name = "username")
	private String username;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "person_id")
	private Person person;

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<Account> friends = new HashSet<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "postedBy")
	private Set<Post> posts = new HashSet<>();


}

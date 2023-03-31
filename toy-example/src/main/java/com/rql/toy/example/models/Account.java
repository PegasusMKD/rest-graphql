package com.rql.toy.example.models;


import com.rql.core.restrict.RQLRestrict;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.util.HashSet;
import java.util.Set;

@Data
@RQLRestrict(value = "person")
@NoArgsConstructor
@Entity(name = "Account")
@Table(name = "account")
@EqualsAndHashCode(exclude = {"posts", "friends", "person", "comments"})
@ToString(exclude = {"posts", "friends", "person", "comments"})
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
	private Set<Comment> comments = new HashSet<>();
}

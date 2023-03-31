package com.rql.toy.example.models;

import com.rest.graphql.rql.core.restrict.RQLRestrict;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@NoArgsConstructor
@Entity(name = "Comment")
@Table(name = "comment")
@RQLRestrict(value = {"post", "account"})
public class Comment {

	@Column(name = "id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	@Id
	private String id;

	@Column(name = "content")
	private String content;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Account account;
}

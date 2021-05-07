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
@Entity(name = "Post")
@Table(name = "post")
@EqualsAndHashCode(exclude = "comments")
@ToString(exclude = "comments")
public class Post {

	@Column(name = "id", length = 32)
	@GeneratedValue(generator = "strategy-uuid")
	@GenericGenerator(name = "strategy-uuid", strategy = "uuid")
	@Id
	private String id;

	@Column(name = "content")
	private String content;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Account postedBy;

	@OneToMany(mappedBy = "post", orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<Comment> comments = new HashSet<>();
}

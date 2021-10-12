package spring.graphql.rest.rql.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentDto {
	private String id;
	private String content;
	private PostDto post;
	private AccountDto account;
}

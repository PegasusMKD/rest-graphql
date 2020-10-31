package spring.graphql.rest.nonoptimized.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class PostDto {
	private String id;
	private String content;
	private AccountDto postedBy;
	private Set<CommentDto> comments = new HashSet<>();
}

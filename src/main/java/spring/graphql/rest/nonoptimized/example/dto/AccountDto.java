package spring.graphql.rest.nonoptimized.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class AccountDto {
	private String id;
	private String username;
	private PersonDto person;
	private Set<AccountDto> friends = new HashSet<>();
	private Set<PostDto> posts = new HashSet<>();
}

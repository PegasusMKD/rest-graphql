package spring.graphql.rest.nonoptimized.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonDto {
	private String id;
	private String fullName;
	private String phoneNumber;
	private AccountDto account;
}

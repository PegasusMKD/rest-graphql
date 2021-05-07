package spring.graphql.rest.nonoptimized.example.processors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResultDto<T> {

	private String parent;
	private List<T> data;

}

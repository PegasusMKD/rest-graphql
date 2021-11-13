package spring.graphql.rest.rql.example.controller.rest;

import lombok.*;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PageRequestByExample<DTO> {
	private DTO example;
	private LazyLoadEvent lazyLoadEvent;

	public Pageable toPageable() {
		return Optional.ofNullable(lazyLoadEvent).map(LazyLoadEvent::toPageable).orElse(null);
	}
}

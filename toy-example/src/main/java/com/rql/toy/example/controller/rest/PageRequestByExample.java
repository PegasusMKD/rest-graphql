package com.rql.toy.example.controller.rest;

import com.rest.graphql.rql.core.dto.LazyLoadEvent;
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

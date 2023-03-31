package com.rql.toy.example.controller.rest;

import com.rql.core.dto.LazyLoadEvent;
import lombok.*;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Data
@Builder
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class PageRequestByExample<DTO> {
	private DTO example;
	private LazyLoadEvent lazyLoadEvent;

	public Pageable toPageable() {
		return Optional.ofNullable(lazyLoadEvent).map(LazyLoadEvent::toPageable).orElse(null);
	}
}

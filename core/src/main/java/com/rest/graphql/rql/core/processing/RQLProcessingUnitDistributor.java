package com.rest.graphql.rql.core.processing;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Component
public class RQLProcessingUnitDistributor {

	private final BeanFactory beanFactory;

	public RQLProcessingUnitDistributor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public RQLProcessingUnit<?> findProcessingUnit(Class<?> _clazz) {
		return beanFactory.getBean("RQLProcessingUnit" + _clazz.getSimpleName(), RQLProcessingUnit.class);
	}

}

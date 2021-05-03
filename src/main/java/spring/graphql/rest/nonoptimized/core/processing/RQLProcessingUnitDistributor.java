package spring.graphql.rest.nonoptimized.core.processing;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

@Component
public class RQLProcessingUnitDistributor {

	private final BeanFactory beanFactory;

	public RQLProcessingUnitDistributor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public <T> RQLProcessingUnit<T> findProcessingUnit(Class<T> _clazz) {
		return beanFactory.getBean("RQLProcessingUnit" + _clazz.getSimpleName(), RQLProcessingUnit.class);
	}


}

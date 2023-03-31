package com.rest.graphql.rql.core.restrict.aop;

import com.rest.graphql.rql.core.restrict.RestrictRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class RestrictAOP {

	private final RestrictRepository restrictRepository;

	public RestrictAOP(RestrictRepository restrictRepository) {
		this.restrictRepository = restrictRepository;
	}

	@Around("@annotation(com.rest.graphql.rql.core.restrict.aop.RQLAOPRestrict)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		String[] attributePaths = (String[]) AOPUtility.getParameterByName(joinPoint, "attributePaths");
		RQLAOPRestrict annotation = (RQLAOPRestrict) AOPUtility.getAnnotation(joinPoint, RQLAOPRestrict.class);

		restrictRepository.filter(annotation.type(), attributePaths);

		return joinPoint.proceed();
	}


}

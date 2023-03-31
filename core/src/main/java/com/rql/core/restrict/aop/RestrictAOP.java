package com.rql.core.restrict.aop;

import com.rql.core.restrict.RestrictRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@RequiredArgsConstructor
public class RestrictAOP {

	private final RestrictRepository restrictRepository;
	private final AOPUtility aopUtility;

	@Around("@annotation(com.rql.core.restrict.aop.RQLAOPRestrict)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		String[] attributePaths = (String[]) aopUtility.getParameterByName(joinPoint, "attributePaths");
		RQLAOPRestrict annotation = (RQLAOPRestrict) aopUtility.getAnnotation(joinPoint, RQLAOPRestrict.class);

		restrictRepository.filter(annotation.type(), attributePaths);

		return joinPoint.proceed();
	}


}

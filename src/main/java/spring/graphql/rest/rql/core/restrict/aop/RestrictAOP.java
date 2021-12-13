package spring.graphql.rest.rql.core.restrict.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import spring.graphql.rest.rql.core.restrict.RestrictRepository;

@Aspect
@Configuration
public class RestrictAOP {

	private final RestrictRepository restrictRepository;

	public RestrictAOP(RestrictRepository restrictRepository) {
		this.restrictRepository = restrictRepository;
	}

	@Around("@annotation(RQLAOPRestrict)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		String[] attributePaths = (String[]) AOPUtility.getParameterByName(joinPoint, "attributePaths");
		RQLAOPRestrict annotation = (RQLAOPRestrict) AOPUtility.getAnnotation(joinPoint, RQLAOPRestrict.class);

		restrictRepository.filter(annotation.type(), attributePaths);

		return joinPoint.proceed();
	}


}

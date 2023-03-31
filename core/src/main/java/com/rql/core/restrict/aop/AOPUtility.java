package com.rql.core.restrict.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@Component
public class AOPUtility {

	public Object getParameterByName(ProceedingJoinPoint proceedingJoinPoint, String parameterName) {
		MethodSignature methodSig = (MethodSignature) proceedingJoinPoint.getSignature();
		Object[] args = proceedingJoinPoint.getArgs();
		String[] parametersName = methodSig.getParameterNames();

		int idx = Arrays.asList(parametersName).indexOf(parameterName);

		if (args.length > idx)
			return args[idx];

		return null;
	}

	public Annotation getAnnotation(ProceedingJoinPoint joinPoint, Class<? extends Annotation> annotation) {
		MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
		return methodSig.getMethod().getAnnotation(annotation);
	}
}

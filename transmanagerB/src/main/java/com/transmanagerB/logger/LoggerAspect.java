package com.transmanagerB.logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggerAspect {
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	static String name = "";
	static String type = "";
	
	@Around("execution(* transmanagerG..controller.*Controller.*(..)) or execution(* transmanagerG..service.*Service.*(..))")
	public Object logPrint(ProceedingJoinPoint joinPoint) throws Throwable {
		type = joinPoint.getSignature().getDeclaringTypeName();
		
		if (type.indexOf("Controller") > -1) {
			name = "Controller  \t:  ";
		}
		else if (type.indexOf("Service") > -1) {
			name = "ServiceImpl  \t:  ";
		}
		
		log.info(name + type + "." + joinPoint.getSignature().getName() + "()");
		return joinPoint.proceed();
	}
}


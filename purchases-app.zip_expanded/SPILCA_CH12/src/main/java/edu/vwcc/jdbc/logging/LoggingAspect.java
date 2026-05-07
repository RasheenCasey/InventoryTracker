package edu.vwcc.jdbc.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

	@Before("execution(* edu.vwcc.jdbc.repo.*.*(..))")
	public void logBefore(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().toShortString();
		logger.info("Entering method: " + methodName);
	}

	@After("execution(* edu.vwcc.jdbc.repo.*.*(..))")
	public void logAfter(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().toShortString();
		logger.info("Exited method: " + methodName);
	}
}

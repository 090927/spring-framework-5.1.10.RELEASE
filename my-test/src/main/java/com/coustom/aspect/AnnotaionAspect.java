package com.coustom.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * DATE 2021-05-01
 *
 */
@Component
@Aspect
public class AnnotaionAspect {


	@Pointcut("execution(* com.coustom.service..*(..))")
	public void aspect(){ }


	@Before("aspect()")
	public void before(JoinPoint joinPoint) {
		System.out.println("befroe 通知" + joinPoint);
	}

	@After("aspect()")
	public void after(JoinPoint joinPoint) {
		System.out.println("after 通知" + joinPoint);
	}
}

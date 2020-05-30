package com.coustom;

import com.coustom.service.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * DATE 2020-05-30
 */
public class MyApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		HelloService helloService = applicationContext.getBean("helloService", HelloService.class);
		helloService.sayHello();
	}
}

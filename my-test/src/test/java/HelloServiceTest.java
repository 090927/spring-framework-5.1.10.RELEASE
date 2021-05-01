import com.coustom.service.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * DATE 2021-05-01
 */
@ContextConfiguration(locations = {"classpath*:applicationContext.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class HelloServiceTest {

	@Resource
	HelloService helloService;


	@Test
	public void testHello() {
		helloService.sayHello();
	}
}

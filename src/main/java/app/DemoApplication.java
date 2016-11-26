package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableAutoConfiguration
public class DemoApplication {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	
	 
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
	
	}
	
	
	
}

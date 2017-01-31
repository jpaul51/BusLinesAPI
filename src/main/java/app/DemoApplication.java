package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@SpringBootApplication
@EnableAutoConfiguration
@EnableOAuth2Sso
public class DemoApplication   extends WebSecurityConfigurerAdapter {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	
	 
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		 
	
	}
	
	
	
}

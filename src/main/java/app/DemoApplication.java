package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;


@SpringBootApplication
@EnableAutoConfiguration
@EnableOAuth2Sso

public class DemoApplication   extends WebSecurityConfigurerAdapter {

	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
	 @Autowired
	  OAuth2ClientContext oauth2ClientContext;
	
	 
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	
	
	}
	
	
	


	 @Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	      .antMatcher("/**")
	      .authorizeRequests()
	        .antMatchers("/", "/getlinesandstops**")
	        .permitAll()
	      .anyRequest()
	        .authenticated();
	   
	  }

	
}

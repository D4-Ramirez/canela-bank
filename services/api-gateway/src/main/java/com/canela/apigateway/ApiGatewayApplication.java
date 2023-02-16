package com.canela.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(ApiGatewayApplication.class);
		ConfigurableEnvironment environment = new StandardEnvironment();
		environment.setDefaultProfiles("development");
		application.setEnvironment(environment);
		application.run(args);
	}

}

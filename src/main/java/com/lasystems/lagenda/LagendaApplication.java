package com.lasystems.lagenda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EntityScan(basePackages = "com.lasystems.lagenda.models")
@EnableJpaRepositories(basePackages = "com.lasystems.lagenda.repository")
public class LagendaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LagendaApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}

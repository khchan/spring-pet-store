package com.khchan;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.khchan")
@EntityScan("com.khchan.petstore.domain")
@EnableJpaRepositories("com.khchan.petstore.repository")
public class TestApplication {
}

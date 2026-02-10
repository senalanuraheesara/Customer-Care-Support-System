package lk.sliit.customer_care_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class CustomerCareSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerCareSystemApplication.class, args);
    }

}

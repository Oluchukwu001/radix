package app.radix.navigationSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class NavigationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NavigationSystemApplication.class, args);
	}

}

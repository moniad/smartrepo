package pl.edu.agh.smart_repo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BackendApplication.class);

        app.setDefaultProperties(Collections
                .singletonMap("server.port", "7777"));
        app.run(args);
    }
}

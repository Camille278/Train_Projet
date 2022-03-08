package org.miage.trainprojet;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TrainApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainApplication.class, args);
    }

    @Bean
    public OpenAPI intervenantAPI(){
        return new OpenAPI().info(new Info()
                .title("Train API")
                .version("1.0")
                .description("Documentation sommaire de l'API"));
    }
}

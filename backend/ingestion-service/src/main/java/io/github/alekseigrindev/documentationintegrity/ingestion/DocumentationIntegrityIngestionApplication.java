package io.github.alekseigrindev.documentationintegrity.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DocumentationIntegrityIngestionApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(
                DocumentationIntegrityIngestionApplication.class
        );
        application.setHeadless(false);
        application.run(args);
    }

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }
}

package gg.watherum.elitebot.configuration;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:app.properties")
@Import({DatabaseConfig.class})
@ComponentScan({"gg.watherum.elitebot"})
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(gg.watherum.elitebot.configuration.Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
//                System.out.println(beanName);
            }

        };
    }

}

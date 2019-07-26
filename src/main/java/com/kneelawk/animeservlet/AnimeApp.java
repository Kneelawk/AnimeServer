package com.kneelawk.animeservlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Created by Kneelawk on 7/26/19.
 */
@SpringBootApplication
public class AnimeApp extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(AnimeApp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AnimeApp.class);
    }
}

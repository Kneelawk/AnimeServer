package com.kneelawk.animeservlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

/**
 * Created by Kneelawk on 7/26/19.
 */
@SpringBootApplication
public class AnimeApp {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AnimeApp.class);
        try {
            application.setDefaultProperties(PropertiesLoaderUtils.loadAllProperties("keystore/keystore.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        application.run(args);
    }
}

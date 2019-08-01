package com.kneelawk.animeservlet.codec;

import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Kneelawk on 7/30/19.
 */
@Configuration
public class MultiPartConfigurer {
    @Bean
    public MultiPartWriter multiPartWriter() {
        return new MultiPartWriter();
    }

    @Bean
    public CodecCustomizer specialEntityCodecCustomizer() {
        return configurer -> {
            System.out.println("Configuring SpecialEntity CodecCustomizer...");
            System.out.println("Configurer: " + configurer.getClass());
            configurer.customCodecs().writer(multiPartWriter());
        };
    }
}

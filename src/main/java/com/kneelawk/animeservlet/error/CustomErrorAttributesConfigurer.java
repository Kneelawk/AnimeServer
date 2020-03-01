package com.kneelawk.animeservlet.error;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Kneelawk on 3/1/20.
 */
@Configuration
public class CustomErrorAttributesConfigurer {
    private final ServerProperties serverProperties;

    public CustomErrorAttributesConfigurer(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    public CustomErrorAttributes errorAttributes() {
        return new CustomErrorAttributes(serverProperties.getError().isIncludeException());
    }
}

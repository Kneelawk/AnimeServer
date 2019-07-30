package com.kneelawk.animeservlet.codec;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Created by Kneelawk on 7/29/19.
 */
@Configuration
public class MultiPartResponseConfiguration implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MultiPartResponseBodyMessageConverter());
    }
}

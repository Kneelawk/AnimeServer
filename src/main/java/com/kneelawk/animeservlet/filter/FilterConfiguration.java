package com.kneelawk.animeservlet.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Kneelawk on 7/27/19.
 */
@Configuration
@PropertySource("classpath:animeserver.properties")
public class FilterConfiguration {
    @Value("${animeserver.filter.type}")
    private FilterType filterType;

    @Value("${animeserver.filter.filename}")
    private Pattern filenameFilter;

    @Value("${animeserver.filter.mimetypes}")
    private List<String> mimeTypes;

    @Bean
    public FileVisibilityFilter getFileVisibilityFilter() {
        return new FileVisibilityFilter(filterType, filenameFilter, mimeTypes);
    }
}

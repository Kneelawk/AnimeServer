package com.kneelawk.animeservlet;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.io.File;

/**
 * Created by Kneelawk on 2/29/20.
 */
public class TestPropertyOverrideContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        File testResourcesDir = new File("test-resources");
        if (!testResourcesDir.exists()) {
            throw new RuntimeException(
                    "Missing runtime test-resources directory! This test may be being run from the wrong location.");
        }

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "animeserver.filesystem.path=" + testResourcesDir.getAbsolutePath());
    }
}

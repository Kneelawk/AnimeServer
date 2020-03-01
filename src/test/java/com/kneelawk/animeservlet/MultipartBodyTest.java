package com.kneelawk.animeservlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Created by Kneelawk on 2/29/20.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestPropertyOverrideContextInitializer.class, classes = AnimeApp.class)
public class MultipartBodyTest {
    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @Before
    public void setup() {
        this.client = WebTestClient.bindToApplicationContext(context).configureClient().build();
    }

    @Test
    public void shouldHandleRangeHeaderOnFiles() {
        client.get()
                .uri("/files/test-file.txt")
                .header("Range", "bytes=0-4")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody(String.class).isEqualTo("Hello");
    }
}

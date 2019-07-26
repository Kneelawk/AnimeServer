package com.kneelawk.animeservlet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Kneelawk on 7/26/19.
 */
@RestController
public class AnimeController {
    @GetMapping("/blah")
    public String blah() {
        return "Hello World!";
    }
}

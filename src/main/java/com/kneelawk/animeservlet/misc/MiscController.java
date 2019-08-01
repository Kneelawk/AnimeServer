package com.kneelawk.animeservlet.misc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Kneelawk on 8/1/19.
 */
@Controller
public class MiscController {
    @GetMapping("/")
    public String getIndex() {
        return "index";
    }
}

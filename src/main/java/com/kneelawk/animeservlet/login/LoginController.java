package com.kneelawk.animeservlet.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by Kneelawk on 1/14/20.
 */
@Controller
public class LoginController {
    @GetMapping("/login/")
    public String getLogin() {
        return "login";
    }

    @PostMapping("/login/")
    public String submitLogin() {
        return "redirect:/";
    }
}

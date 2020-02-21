package com.kneelawk.animeservlet.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignUpController {
    @GetMapping("/signup/")
    public String getSignUp() {
        return "signup";
    }

    @PostMapping("/signup/")
    public String submitSignUp() {
        return "redirect:/";
    }
}

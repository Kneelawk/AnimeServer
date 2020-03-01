package com.kneelawk.animeservlet.login;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created by Kneelawk on 1/14/20.
 */
@Controller
public class LoginController {
    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @ModelAttribute("_csrf")
    public Mono<CsrfToken> addCsrfToken(ServerWebExchange exchange) {
        return exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
    }
}

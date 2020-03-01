package com.kneelawk.animeservlet.login;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created by Kneelawk on 3/1/20.
 */
@Controller
public class LogoutController {
    @GetMapping("/logout")
    public String getLogout(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/";
        } else {
            return "logout";
        }
    }

    @ModelAttribute("_csrf")
    public Mono<CsrfToken> addCsrfToken(ServerWebExchange exchange) {
        return exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
    }
}

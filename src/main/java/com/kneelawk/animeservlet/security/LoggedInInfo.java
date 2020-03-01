package com.kneelawk.animeservlet.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.Principal;

/**
 * Created by Kneelawk on 3/1/20.
 */
public class LoggedInInfo {
    private static final LoggedInInfo NOT_LOGGED_IN = new LoggedInInfo(false, null);

    private final boolean loggedIn;
    private final String name;

    public LoggedInInfo(boolean loggedIn, String name) {
        this.loggedIn = loggedIn;
        this.name = name;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getName() {
        return name;
    }

    public static LoggedInInfo fromPrincipal(Principal principal) {
        if (principal != null) {
            return new LoggedInInfo(true, principal.getName());
        }
        return NOT_LOGGED_IN;
    }

    public static LoggedInInfo fromAuthentication(Authentication authentication) {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return new LoggedInInfo(true, authentication.getName());
        }
        return NOT_LOGGED_IN;
    }
}

package com.kneelawk.animeservlet.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Created by Kneelawk on 3/1/20.
 */
@ControllerAdvice
public class LoggedInAdvice {
    @ModelAttribute("login")
    public LoggedInInfo getLoggedInInfo(Authentication authentication) {
        return LoggedInInfo.fromAuthentication(authentication);
    }
}

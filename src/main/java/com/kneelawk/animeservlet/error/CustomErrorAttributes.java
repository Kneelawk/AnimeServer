package com.kneelawk.animeservlet.error;

import com.kneelawk.animeservlet.security.LoggedInInfo;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * Created by Kneelawk on 3/1/20.
 */
public class CustomErrorAttributes extends DefaultErrorAttributes {
    public CustomErrorAttributes(boolean includeException) {
        super(includeException);
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Map<String, Object> attributes = super.getErrorAttributes(request, includeStackTrace);

        applyLoggedInInfo(attributes, request);

        return attributes;
    }

    private void applyLoggedInInfo(Map<String, Object> attributes, ServerRequest request) {
        attributes.put("login", LoggedInInfo.fromPrincipal(request.principal().block()));
    }
}

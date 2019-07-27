package com.kneelawk.animeservlet.fileserver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Kneelawk on 7/27/19.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileServerNotFoundException extends RuntimeException {
    public FileServerNotFoundException() {
    }

    public FileServerNotFoundException(String message) {
        super(message);
    }

    public FileServerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileServerNotFoundException(Throwable cause) {
        super(cause);
    }
}

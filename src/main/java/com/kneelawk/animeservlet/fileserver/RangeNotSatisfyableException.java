package com.kneelawk.animeservlet.fileserver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Kneelawk on 7/29/19.
 */
@ResponseStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
public class RangeNotSatisfyableException extends RuntimeException {
    public RangeNotSatisfyableException() {
    }

    public RangeNotSatisfyableException(String message) {
        super(message);
    }

    public RangeNotSatisfyableException(String message, Throwable cause) {
        super(message, cause);
    }

    public RangeNotSatisfyableException(Throwable cause) {
        super(cause);
    }
}

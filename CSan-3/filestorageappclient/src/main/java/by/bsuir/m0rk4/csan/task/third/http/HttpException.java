package by.bsuir.m0rk4.csan.task.third.http;

import org.springframework.web.bind.annotation.ExceptionHandler;

public class HttpException extends Exception {
    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}

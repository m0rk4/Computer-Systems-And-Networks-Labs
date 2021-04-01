package by.bsuir.m0rk4.csan.task.third.filestorageapp.exception;

public class FileException extends RuntimeException {
    public FileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileException(String message) {
        super(message);
    }
}

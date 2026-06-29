package app.radix.navigationSystem.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
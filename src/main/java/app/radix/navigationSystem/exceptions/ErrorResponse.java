package app.radix.navigationSystem.exceptions;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final int status;       // e.g., 404
    private final String error;    // e.g., "Not Found"
    private final String message;  // Your custom message
    private final Instant timestamp;
}

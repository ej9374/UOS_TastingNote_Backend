package groomton_univ.tasting_note.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final int status;
    private final String message;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }
}
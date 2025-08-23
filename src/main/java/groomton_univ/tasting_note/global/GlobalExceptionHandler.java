package groomton_univ.tasting_note.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@RestControllerAdvice // 모든 @RestController에서 발생하는 예외를 처리
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 상 유효하지 않은 요청에 대한 예외 처리 (ex 중복된 닉네임)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Bad Request from client: {}", ex.getMessage()); // 서버 로그에는 경고 수준으로 기록
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @Valid 어노테이션을 통한 유효성 검사 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // 가장 첫 번째 에러 메시지를 가져와서 응답으로 사용
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation failed: {}", errorMessage);
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 처리하지 못한 모든 예외에 대한 처리 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception occurred: ", ex); // 서버 로그에는 에러 수준으로 상세히 기록
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
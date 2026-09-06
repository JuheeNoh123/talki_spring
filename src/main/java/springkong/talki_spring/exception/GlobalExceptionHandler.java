package springkong.talki_spring.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(NotFoundException e) {
        return ResponseEntity
                .status(404)
                .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<?> handleDuplicateUser(DuplicateUserException e) {
        return ResponseEntity
                .status(409) // 회원 중복은 보통 409 Conflict
                .body(new ErrorResponse("DUPLICATE_USER", e.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<?> handleInvalidPasswordUser(InvalidPasswordException e) {
        return ResponseEntity
                .status(409) // 회원 중복은 보통 409 Conflict
                .body(new ErrorResponse("InvalidPassword", e.getMessage()));
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse("VALIDATION_ERROR",
                        e.getBindingResult().getFieldError().getDefaultMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidJson(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse("INVALID_JSON", "요청 JSON 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse("MISSING_PARAMETER", e.getParameterName() + " 파라미터가 필요합니다."));
    }

    // =========================
    // HTTP Errors
    // =========================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(405)
                .body(new ErrorResponse("METHOD_NOT_ALLOWED", e.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleUnsupportedMedia(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity
                .status(415)
                .body(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", e.getMessage()));
    }

    // =========================
    // Security Errors
    // =========================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(AuthenticationException e) {
        return ResponseEntity
                .status(401)
                .body(new ErrorResponse("UNAUTHORIZED", "인증이 필요합니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(403)
                .body(new ErrorResponse("FORBIDDEN", "접근 권한이 없습니다."));
    }

    // =========================
    // Server Errors
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace(); // 서버 로그 확인용

        return ResponseEntity
                .status(500)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }


}

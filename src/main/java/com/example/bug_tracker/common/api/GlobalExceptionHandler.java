package com.example.bug_tracker.common.api;

import com.example.bug_tracker.bug.exception.BugNotFoundException; // NotFound例外
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus; // 404指定
import org.springframework.http.ResponseEntity; // レスポンス組み立て
import org.springframework.web.bind.annotation.ExceptionHandler; // 例外ハンドラ
import org.springframework.web.bind.annotation.RestControllerAdvice; // Controller横断
import java.util.List; // details用

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BugNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BugNotFoundException ex) {
        log.info("Bug not found. id={}", ex.getId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "NOT_FOUND",
                        "指定したバグが見つかりませんでした",
                        List.of(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "予期しないエラーが発生しました",
                        List.of("詳細はサーバーログを参照してください")));
    }

    public record ErrorResponse(String code, String message, List<String> details) {
    }
}
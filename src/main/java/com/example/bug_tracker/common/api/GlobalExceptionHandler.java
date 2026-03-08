package com.example.bug_tracker.common.api;

import com.example.bug_tracker.bug.exception.BugNotFoundException; // NotFound例外
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus; // 404指定
import org.springframework.http.ResponseEntity; // レスポンス組み立て
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler; // 例外ハンドラ
import org.springframework.web.bind.annotation.RestControllerAdvice; // Controller横断

import java.util.List; // details用
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // 404例外
        // 存在しないID/Bug Not Found
        @ExceptionHandler(BugNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(BugNotFoundException ex) {
                log.info("Bug not found. id={}", ex.getId());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(
                                                "NOT_FOUND",
                                                "指定したバグが見つかりませんでした",
                                                List.of(ex.getMessage())));
        }

        // 400系例外
        // Validation失敗：400 + VALIDATION_ERROR
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleNotValid(MethodArgumentNotValidException ex) {

                // details：フィールド別エラーを「field message」形式で並べる
                List<String> details = ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> fe.getDefaultMessage())
                                .collect(Collectors.toList());

                // INFO：クライアント入力起因の想定内エラー
                log.info("Validation failed. details={}", details);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse(
                                                "VALIDATION_ERROR",
                                                "入力値が不正です",
                                                details));
        }

        // JSON不正/enum不正など：400 + INVALID_JSON
        @ExceptionHandler(HttpMessageNotReadableException.class) // 400例外
        public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {

                // INFO：想定内（壊れたJSON/enum不正）
                // ※内部例外の生メッセージは漏洩リスクがあるので、詳細は固定文言にする
                log.info("Invalid JSON request body. cause={}", ex.getMostSpecificCause().getClass().getSimpleName());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse(
                                                "INVALID_JSON",
                                                "リクエストJSONの形式が不正です",
                                                List.of("JSONの形式、またはenumの値を確認してください")

                                ));
        }

        // 500例外
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
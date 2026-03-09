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
        // Logger定義：例外処理専用ログ
        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // 404例外： 存在しないID指定
        @ExceptionHandler(BugNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(BugNotFoundException ex) {
                // NotFoundログ
                log.info("GlobalExceptionHandler#handleNotFound. id={}", ex.getId());

                // 404 + 統一形式で返す
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(
                                                "NOT_FOUND",
                                                "指定したバグが見つかりませんでした",
                                                List.of(ex.getMessage())));
        }

        // 400例外：Validation失敗（クライアント入力起因の想定内エラー）
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleNotValid(MethodArgumentNotValidException ex) {

                // details作成：DTO側のdefaultMessageのみ採用
                List<String> details = ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> fe.getDefaultMessage())
                                .collect(Collectors.toList());

                // Validation failedログ
                log.info("GlobalExceptionHandler#handleNotValid. errorCount={}", details.size());

                // 400 + 統一形式で返す
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse(
                                                "VALIDATION_ERROR",
                                                "入力値が不正です",
                                                details));
        }

        // 400例外：JSON形式不正 / enum不正（クライアント起因の想定内エラー）
        @ExceptionHandler(HttpMessageNotReadableException.class) // 400例外
        public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {

                // causeの型名だけ出し、内部メッセージ本文は出しすぎない
                log.info("GlobalExceptionHandler#handleNotReadable. cause={}",
                                ex.getMostSpecificCause().getClass().getSimpleName());

                // 400 + 統一形式で返す
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse(
                                                "INVALID_JSON",
                                                "リクエストJSONの形式が不正です",
                                                List.of("JSONの形式、またはenumの値を確認してください")

                                ));
        }

        // 500例外（想定外障害）
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleInternal(Exception ex) {

                // ERROR：調査対象なのでstacktrace付きで残す（ログ）
                log.error("GlobalExceptionHandler#handleInternal. unexpected error", ex);

                // 500 + 統一形式で返す
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse(
                                                "INTERNAL_ERROR",
                                                "予期しないエラーが発生しました",
                                                List.of("詳細はサーバーログを参照してください")));
        }

        public record ErrorResponse(String code, String message, List<String> details) {
        }
}
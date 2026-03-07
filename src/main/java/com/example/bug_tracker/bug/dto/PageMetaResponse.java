package com.example.bug_tracker.bug.dto;

public record PageMetaResponse(
                int page, // 現在ページ
                int size, // 1ページ件数
                long totalElements, // 総件数
                int totalPages, // 総ページ数
                boolean hasNext, // 次ページ有無
                boolean hasPrevious // 前ページ有無
) {

}

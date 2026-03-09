# Bug-Tracker 要件定義書（v0.1）

## 1. システム概要

- 不具合（Bug）を登録・追跡し、作業状況（status/priority）を管理する。
- 手作業の記録をAPI化し、登録・検索の効率化が目的。
- 方式：REST API（JSON）で提供（画面は後回し）。

## 2. 業務要件（利用シーン）

- シーン1：不具合を発見した担当者が、Bugを登録する。
- シーン2：担当者が、Bugの一覧から状況を確認する。
- シーン3：担当者が、Bugを個別で指定して詳細を確認する。
- シーン4：担当者が、指定した個別Bugを新しい内容で更新する。
- シーン5：担当者が、指定したidのBugを削除する。

## 3. 納品対象（成果物）

- アプリ本体（Spring Boot）
- ドキュメント（docs配下）：要件/API/エラー
- 起動・動作確認手順（README）

## 4. 機能要件

### 4.1 MUST

- Bug API（CRUD）
  - 作成：POST /api/bugs
  - 一覧：GET /api/bugs
  - 個別：GET /api/bugs/{id}
  - 更新：PUT /api/bugs/{id}
  - 削除：DELETE /api/bugs/{id}
- 入力チェック
  - title必須（空は400）
- エラー契約（統一形式）
  - 400：VALIDATION_ERROR / INVALID_JSON
  - 404：NOT_FOUND（/{id}指定のGET/PUT/DELETEで対象が存在しない場合）
- status/priority 未指定時は OPEN/LOW をサーバ側で補完する

### 4.2 SHOULD

- 並び順：createdAt の降順（DB永続化後に確定）

### 4.3 対象外（NOT）

- リッチなフロントUI（フロントはThymeleafで最小実装）
- マイクロサービス化 / 過剰なアーキテクチャ

## 5. 非機能要件（v0）

- ログ：
  - Controller / Service の操作開始・主要結果は INFO で記録する
  - 400（VALIDATION_ERROR / INVALID_JSON）および 404（NOT_FOUND）は想定内エラーとして INFO で記録する
  - 500（INTERNAL_ERROR）のみ想定外障害として ERROR で記録し、スタックトレースを残す
  - request body 全文、個人情報、巨大 payload、内部例外の生メッセージはログへ出しすぎない

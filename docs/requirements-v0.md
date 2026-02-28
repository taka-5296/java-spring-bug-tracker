# Bug-Tracker 要件定義書（v0）

## 1. システム概要
- 不具合（Bug）を登録・追跡し、作業状況（status/priority）を管理する。
- 手作業の記録をAPI化し、登録・検索の効率化が目的。
- 方式：REST API（JSON）で提供（画面は後回し）。

## 2. 業務要件（利用シーン）
- シーン1：不具合を発見した担当者が、Bugを登録する。
- シーン2：担当者が、Bugの一覧から状況を確認する。
- シーン3：担当者が、Bugを個別で指定して詳細を確認する。
  ※更新/削除はCRUD完成後に追加。

## 3. 納品対象（成果物）
- アプリ本体（Spring Boot）
- ドキュメント（docs配下）：要件/API/エラー（v0）
- 起動・動作確認手順（README）

## 4. 機能要件
### 4.1 MUST（v0で必ず）
- Bug API（現状）
  - 作成：POST /api/bugs
  - 一覧：GET /api/bugs
  - 個別：GET /api/bugs/{id}
- 入力チェック
  - title必須（空は400）
- エラー契約（統一形式）
  - 400：VALIDATION_ERROR / INVALID_JSON
  - 404：NOT_FOUND（GET /api/bugs/{id} で対象が存在しない場合）
- status/priority 未指定時は OPEN/LOW をサーバ側で補完する

### 4.2 SHOULD（v0以降に追加）
- Bug CRUD 完成
  - 更新：PUT /api/bugs/{id}
  - 削除：DELETE /api/bugs/{id}
- 並び順：createdAt の降順（DB永続化後に確定）

### 4.3 対象外（NOT）
- リッチなフロントUI（フロントはThymeleafで最小実装）
- マイクロサービス化 / 過剰なアーキテクチャ

## 5. 非機能要件（v0）
- ログ：Controller/Service到達はINFO、例外はERROR（機微情報は出しすぎない）
- 監視：GET /health で生存確認
- 権限：v0では未実装（Week5でUSER/ADMIN）
- データ保持：v0は物理削除（論理削除は将来検討）
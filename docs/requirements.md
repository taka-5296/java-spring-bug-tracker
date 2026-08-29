# Bug Tracker 要件定義書

## 1. 目的

Bugを登録・検索・参照・更新・削除できる小規模な不具合管理アプリケーションを提供する。

本プロジェクトでは、Java 17 / Spring Boot / PostgreSQLを用いて、Controller / Service / Repositoryを分離したWebアプリケーションとして実装する。

## 2. 現在のスコープ

### MUST

- Bug CRUD
  - `POST /api/bugs`
  - `GET /api/bugs`
  - `GET /api/bugs/{id}`
  - `PUT /api/bugs/{id}`
  - `DELETE /api/bugs/{id}`
- 一覧検索
  - `status`
  - `priority`
  - `keyword`
  - 複数条件はAND
- ページング
  - `page`
  - `size`
  - 一覧レスポンスは `items + meta`
- 入力Validation
  - `title` 必須
  - `title` 最大200文字
- `status` / `priority` 未指定時のデフォルト補完
  - `status=OPEN`
  - `priority=LOW`
- PostgreSQLへのBug・User永続化
- 統一エラーレスポンス
  - `VALIDATION_ERROR`
  - `INVALID_JSON`
  - `NOT_FOUND`
  - `INTERNAL_ERROR`
- 自動テスト
  - Service単体
  - Service / Repository / PostgreSQL結合
  - Controller HTTP境界
  - Security結合
- GitHub ActionsによるCI

### SHOULD

- 一覧は `createdAt` 降順とする
- 開発用DBとテスト用DBを分離する
- devでは既存schemaとの整合を検証し、testではテストごとにschemaを作成・破棄する

## 3. Security

認証方式は Spring Security の form login + Session を採用する。

- `/health` は未認証でも利用可能
- `/api/bugs/**` は認証対象
- ロールは `ROLE_USER` / `ROLE_ADMIN`
- `ROLE_USER`
  - Bugの作成、一覧・個別参照、更新を許可する
  - Bug削除は許可しない
- `ROLE_ADMIN`
  - Bugの作成、一覧・個別参照、更新、削除を許可する
- `DELETE /api/bugs/{id}` は `ROLE_ADMIN` のみに許可する
- 未認証で保護対象へアクセスした場合は `302 Found` でログイン画面へリダイレクトする
- form login の認証失敗時はログイン画面へ戻し、認証失敗であることを表示する
- 認証情報はPostgreSQLの `users` テーブルから取得する
- passwordはBCrypt hashとして保存し、平文passwordをDBへ保存しない
- dev用のUSER / ADMINは `docs/db/users.sql` で投入する
- testではdev用seedに依存せず、テストコード側で必要なユーザーを作成する

## 4. 非機能要件

### ログ

- 正常な主要処理はINFOで記録する
- 400 / 404など想定内の入力・業務エラーはINFOを基本とする
- 予期しない500はERRORでstack traceを残す
- request body全文、認証情報、内部例外の生メッセージを不用意に出力しない

### 再現性

- Maven Wrapperでビルド・テストできる
- PostgreSQLはDockerで再現できる
- READMEから起動・疎通確認・主要操作確認へ到達できる
- RunbookからBug CRUD、検索、ページング、認証・認可、DB確認を再現できる
- CIで同じ自動テストを実行できる

## 5. 対象外

- マイクロサービス化
- 過剰なレイヤ分割
- 高度な検索基盤
- リッチなフロントエンド
- 現段階で利用しない複雑なインフラ構成

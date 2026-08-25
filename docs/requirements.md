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
- PostgreSQLへの永続化
- 統一エラーレスポンス
  - `VALIDATION_ERROR`
  - `INVALID_JSON`
  - `NOT_FOUND`
  - `INTERNAL_ERROR`
- 自動テスト
  - Service単体
  - Service / Repository / PostgreSQL結合
  - Controller HTTP境界
- GitHub ActionsによるCI

### SHOULD

- 一覧は `createdAt` 降順とする
- 開発用DBとテスト用DBを分離する
- devでは既存schemaとの整合を検証し、testではテストごとにschemaを作成・破棄する

## 3. Security

Spring Securityは導入済みだが、最終的な認証・認可契約はG07で確定する。

予定している要件：

- `/health` は未認証でも利用可能
- `/api/bugs/**` は認証対象
- ロールは `ROLE_USER` / `ROLE_ADMIN`
- Bug削除はADMINのみ許可
- usersのDB永続化はSecurity実装の後続段階で行う

### 未決定事項

未認証でREST APIへアクセスした場合の契約は、次のどちらを採用するかG07で確定する。

- APIとして `401 Unauthorized` を返す
- form loginとしてログイン画面へリダイレクトする

この判断が確定するまでは、401/302のどちらかを固定契約として扱わない。

## 4. 非機能要件

### ログ

- 正常な主要処理はINFOで記録する
- 400 / 404など想定内の入力・業務エラーはINFOを基本とする
- 予期しない500はERRORでstack traceを残す
- request body全文、認証情報、内部例外の生メッセージを不用意に出力しない

### 再現性

- Maven Wrapperでビルド・テストできる
- PostgreSQLはDockerで再現できる
- READMEから起動・疎通確認・テスト実行へ到達できる
- CIで同じテストを自動実行できる

## 5. 対象外

- マイクロサービス化
- 過剰なレイヤ分割
- 高度な検索基盤
- リッチなフロントエンド
- 現段階で利用しない複雑なインフラ構成

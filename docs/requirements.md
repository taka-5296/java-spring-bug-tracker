# Bug Tracker 要件定義書

## 1. 目的

Bugを登録・検索・参照・更新・削除できる小規模な不具合管理アプリケーションを提供する。

最終構成では、Java 17 / Spring Boot / PostgreSQLによるbackendと、TypeScript / React / Next.jsによるfrontendを組み合わせる。
REST API、業務ロジック、Validation、Security、DBアクセスはSpring Boot側を正とし、frontendは既存REST APIを利用する。

## 2. 要求スコープ

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
- Spring Securityによる認証・認可
  - form login + Session
  - `ROLE_USER` / `ROLE_ADMIN`
  - DELETEはADMIN限定
  - CSRF保護
- TypeScript / React / Next.jsによるfrontend
  - Login
  - Bug一覧
  - status / priority / keyword検索
  - ページング
  - Bug作成・更新・削除
  - Validation / Error表示
  - USER / ADMINの権限差を考慮した画面制御
  - Spring Boot REST APIを利用し、DBへ直接接続しない
- 自動テスト
  - Service単体
  - Service / Repository / PostgreSQL結合
  - Controller HTTP境界
  - Security結合
  - frontend導入後は重要なfrontend処理の最小テストまたはbuild確認を追加する
- GitHub ActionsによるCI
- Docker / Docker Composeによる再現環境
  - 採用したfrontend / backend / db構成に必要なserviceを再現できる

### SHOULD

- 一覧は `createdAt` 降順とする
- 開発用DBとテスト用DBを分離する
- devでは既存schemaとの整合を検証し、testではテストごとにschemaを作成・破棄する
- frontendとbackendの責務を分離し、同じ業務ロジックを二重実装しない
- frontendの技術機能はBug Trackerに必要な範囲から段階的に導入する

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
- 未認証で保護対象へアクセスした場合は、現行backendでは `302 Found` でログイン画面へリダイレクトする
- form login の認証失敗時はログイン画面へ戻し、認証失敗であることを表示する
- 認証情報はPostgreSQLの `users` テーブルから取得する
- passwordはBCrypt hashとして保存し、平文passwordをDBへ保存しない
- dev用のUSER / ADMINは `docs/db/users.sql` で投入する
- testではdev用seedに依存せず、テストコード側で必要なユーザーを作成する
- POST / PUT / DELETEではCSRF保護を維持する
- frontend側の表示制御だけに認可を依存せず、最終的な権限制御はSpring Security側で行う
- frontendとbackendを別originで実行する場合は、CORS、Cookie送信、CSRF token連携方式を実装前に設計・確認する
- JWT / OAuth等への認証方式変更は、必要性が明確になるまで行わない

## 4. 非機能要件

### ログ

- 正常な主要処理はINFOで記録する
- 400 / 404など想定内の入力・業務エラーはINFOを基本とする
- 予期しない500はERRORでstack traceを残す
- request body全文、認証情報、内部例外の生メッセージを不用意に出力しない

### 再現性

- Maven Wrapperでbackendをビルド・テストできる
- frontend導入後はdocumented commandでfrontendをbuildまたは同等確認できる
- PostgreSQLはDockerで再現できる
- 最終構成ではDocker Composeで採用した必要serviceを起動できる
- READMEから起動・疎通確認・主要操作確認へ到達できる
- RunbookからBug CRUD、検索、ページング、認証・認可、DB確認を再現できる
- frontend導入後はbrowserから主要操作と権限差を再現できる
- CIで必要な自動テスト・build確認を実行できる

## 5. 対象外

- マイクロサービス化
- 過剰なレイヤ分割
- 高度な検索基盤
- Spring Bootを置き換えるNext.jsバックエンド化
- Next.jsからPostgreSQLへの直接接続
- Spring Bootと同じ業務APIのRoute Handler / Server Actionへの重複実装
- Redux等、必要性を確認していない追加状態管理ライブラリ
- Next.jsの高度なServer機能、cache、streaming、最適化の先行導入
- 必要性のないJWT / OAuth等への認証方式変更
- UIデザインの作り込み
- 現段階で利用しない複雑なインフラ構成

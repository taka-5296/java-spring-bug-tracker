# Bug Tracker 設計判断

## D-001 status / priority のデフォルト補完

**Status:** Accepted

- 未指定の `status` は `OPEN`
- 未指定の `priority` は `LOW`
- 補完はServiceで行う

理由：
APIクライアント側へ必須補完を強制せず、業務上の初期状態をサーバ側で一貫して決定するため。

## D-002 現行永続化はbugsテーブルのみ

**Status:** Superseded by D-009

Security実装前はBug CRUDへ集中し、usersのDB永続化を後続とする判断を採用した。

現在はD-009によりusersのPostgreSQL永続化を実装済みであり、本判断は現行構成には適用しない。

## D-003 ID採番はIDENTITY

**Status:** Accepted

`BugEntity.id` はDBのIDENTITYによる採番を使用する。

理由：
現在のPostgreSQL + JPA構成で単純かつ十分であるため。

## D-004 エラーレスポンスを統一する

**Status:** Accepted

アプリケーション例外は原則として次の構造で返す。

- code
- message
- details

400 / 404 / 500の主要エラーを `GlobalExceptionHandler` でHTTPレスポンスへ変換する。

Security Filter由来の認証・認可処理はSpring Security側で扱い、`GlobalExceptionHandler` と同一経路・同一形式で処理することは前提としない。

## D-005 レイヤ責務

**Status:** Accepted

- Controller: HTTP request / response境界
- Service: 業務判断、トランザクション境界
- Repository: 永続化・検索
- Entity: DB永続化モデル
- DTO: API入力・出力モデル

`BugEntity -> BugResponse` の変換は現在の規模ではController側に置く。
専用Mapperは重複や変換規模が増えた場合に検討する。

## D-006 設定ファイルとDB環境を分離する

**Status:** Accepted

設定ファイルは `.properties` 形式へ統一する。

- `application.properties`: 共通設定
- `application-dev.properties`: dev
- `application-test.properties`: test

DBは次のように分離する。

- dev: `bug_tracker`, `ddl-auto=validate`
- test: `bug_tracker_test`, `ddl-auto=create-drop`

理由：
小規模な設定ではpropertiesの方が構造が単純で、dev/testを同一DBへ接続する事故を避けやすいため。

## D-007 Securityの未認証レスポンス

**Status:** Accepted

認証方式は Spring Security の form login + Session を採用する。

- `/health` は未認証でも利用可能とする
- `/api/bugs/**` は認証対象とする
- ロールは `ROLE_USER` / `ROLE_ADMIN` とする
- `ROLE_USER` はBugの作成・参照・更新を許可する
- `ROLE_ADMIN` はBugの作成・参照・更新・削除を許可する
- `DELETE /api/bugs/{id}` は `ROLE_ADMIN` のみに許可する
- 未認証で保護対象へアクセスした場合は `302 Found` で `/login` へリダイレクトする
- form loginの認証失敗時はログイン画面へ戻し、認証失敗を表示する
- 認証済み `ROLE_USER` がADMIN限定操作を行った場合は `403 Forbidden` とする

理由：
Spring Security標準のSession認証を利用し、認証状態をサーバ側で管理する最小構成で認証・認可を実現するため。
JWT等のtoken発行・更新基盤を追加せず、現在必要なUSER / ADMINの権限制御とCSRF保護をSpring Securityの標準機能で扱う。

## D-008 docs構成を簡素化する

**Status:** Accepted

旧構成：

- requirements-v0.md
- api-spec-v0.md
- error-spec-v0.md
- er-v0.md
- schema-v0.md
- decisions-v0.md
- test-design-v0.md
- operations.md

新構成：

- requirements.md
- api-spec.md
- data-model.md
- decisions.md
- test-design.md
- operations.md

`error-spec` は `api-spec` へ、`er` は `data-model` へ統合する。

理由：
内容の重複と更新漏れを減らすため。

## D-009 users認証情報をPostgreSQLへ永続化する

**Status:** Accepted

Securityの認証元として、`users` テーブルを使用する。

最小項目は次とする。

- id
- username
- password_hash
- role
- enabled

usernameには一意制約を付ける。
passwordはBCryptでhash化した値だけを保存し、平文passwordをDBへ保存しない。
roleはDBでは `USER` / `ADMIN` として保持し、Spring Security上では `ROLE_USER` / `ROLE_ADMIN` として扱う。
dev用の初期USER / ADMINは `docs/db/users.sql` で投入し、SQLにはBCrypt済みhashのみを保存する。
testではdev用seedへ依存せず、テスト側で必要なユーザーを用意する。
現段階ではusersとbugsの関連付けは行わない。

理由：
DB-backed UserDetailsServiceへ安全かつ最小構成で移行し、現在必要のないユーザー管理機能やテーブル関連を追加しないため。

## D-010 frontendはTypeScript / React / Next.jsを使用し、Spring Bootをbackendの正とする

**Status:** Accepted

最終的なbrowser向けfrontendにはTypeScript / React / Next.jsを使用する。

責務は次のように分離する。

- frontend
  - 画面表示
  - routing
  - form入力
  - Bug一覧・検索・ページング・CRUDの操作UI
  - Validation / Errorの表示
  - Login状態とUSER / ADMINに応じた表示制御
  - Spring Boot REST APIとのHTTP通信
- Spring Boot
  - REST API
  - 業務ロジック
  - Bean Validation
  - 認証・認可
  - DBアクセス
  - PostgreSQL永続化

Next.jsからPostgreSQLへ直接接続しない。
Spring Bootに存在する業務APIをNext.jsのRoute HandlerやServer Actionへ重複実装しない。
frontend側の表示制御だけを認可の最終防御とせず、権限制御の正はSpring Securityとする。

認証方式は現行のform login + Session / Cookie / CSRFを維持する。
frontendとbackendを別originで実行する場合のCORS、Cookie送信、CSRF token連携などの詳細な接続方式は、frontend実装前に設計して固定する。
必要性が確認されない限りJWT / OAuthへ変更しない。

理由：
既存のSpring Boot REST API、Security、DB責務を維持しながらbrowser向けfrontendを追加し、同じ業務ロジックや認証責務をfrontend側へ二重実装しないため。
また、TypeScript / React / Next.jsの学習対象を実際のBug Tracker画面へ結び付けつつ、不要なbackend再構築や認証方式変更を避けるため。

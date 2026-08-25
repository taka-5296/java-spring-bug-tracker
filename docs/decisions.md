# Bug Tracker 設計判断

## D-001 status / priority のデフォルト補完

**Status:** Accepted

- 未指定の `status` は `OPEN`
- 未指定の `priority` は `LOW`
- 補完はServiceで行う

理由：
APIクライアント側へ必須補完を強制せず、業務上の初期状態をサーバ側で一貫して決定するため。

## D-002 現行永続化はbugsテーブルのみ

**Status:** Accepted

Security実装前はBug CRUDへ集中し、usersのDB永続化は後続とする。

理由：
現在使わないuser関連schemaを先行実装して手戻りを増やさないため。

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
本アプリケーションは後続で最小Thymeleaf画面を持つ予定であり、ブラウザ利用時に未認証ユーザーをログイン画面へ誘導する単純な認証フローを採用するため。
REST APIクライアントに対して401を返す方式よりもブラウザ向け挙動を優先することを、この段階の設計判断として明示する。

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

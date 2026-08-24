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

Security Filter由来の401 / 403についてはD-007の決定後に別途契約を確定する。

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

**Status:** Open

G07開始前に次を確定する。

候補A:
REST APIとして未認証は `401 Unauthorized`、権限不足は `403 Forbidden` を返す。

候補B:
form loginを採用し、未認証時はログイン画面へリダイレクトする。

README、requirements、API仕様で異なる前提を持たないよう、採用案決定後に関連文書を同一PRで更新する。

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

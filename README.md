# Bug Tracker（不具合管理アプリ）

Spring BootとPostgreSQLで構築した、不具合（Bug）の登録・検索・参照・更新・削除を行うWebアプリケーションです。

Controller / Service / Repositoryを分離し、Bean Validation、統一エラーレスポンス、PostgreSQL永続化、Spring Security、自動テスト、GitHub ActionsによるCIを備えています。

## 主な機能

- ヘルスチェック：`GET /health`
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
  - 複数条件のAND検索
- `page` / `size` によるページング
- `title` の入力Validation
- `status` / `priority` 未指定時の `OPEN` / `LOW` 補完
- PostgreSQL / JPAによるBug・User永続化
- Spring Securityによるform login + Session認証
- USER / ADMINの権限制御
  - USER：作成・参照・更新
  - ADMIN：作成・参照・更新・削除
- 統一エラーレスポンス
- Service単体テスト、DB結合テスト、Controller HTTP境界テスト、Security結合テスト
- GitHub Actionsによる自動テスト

APIのRequest / Response / HTTPステータス / エラー契約は [API仕様](docs/api-spec.md) を参照してください。

## 技術スタック

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- Bean Validation
- Spring Security
- PostgreSQL 16
- Maven Wrapper
- JUnit
- Mockito
- MockMvc
- GitHub Actions
- Docker（PostgreSQLのローカル実行）

## Quick Start

### 前提

- Java 17
- Docker Desktop
- Windows PowerShell

### 1. PostgreSQLを起動する

初回：

```powershell
docker run --name bug-tracker-postgres `
  -e POSTGRES_DB=bug_tracker `
  -e POSTGRES_USER=bug_user `
  -e POSTGRES_PASSWORD=bug_pass `
  -p 5432:5432 `
  -d postgres:16
```

既存コンテナを利用する場合：

```powershell
docker start bug-tracker-postgres
```

### 2. 開発用schemaと初期ユーザーを適用する

```powershell
Get-Content .\docs\db\bugs.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker

Get-Content .\docs\db\users.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

devプロファイルでは `ddl-auto=validate` を使用し、JPA Entityと既存schemaの整合を確認します。

### 3. アプリを起動する

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 4. 疎通確認

別のPowerShellで実行します。

```powershell
curl.exe "http://localhost:8080/health"
```

期待結果：

```text
OK
```

### 5. 主な機能をまとめて確認する

アプリ起動中に次を実行します。

```powershell
.\scripts\smoke.ps1
```

このスクリプトでは、未認証アクセス、DBユーザーのログイン、BugのPOST / GET / PUT / DELETE、検索、ページング、USER / ADMINの権限制御を実HTTPで確認します。

各操作の意味、期待結果、DB確認、障害切り分けは [Runbook](docs/operations.md) を参照してください。

## ローカル認証アカウント

`docs/db/users.sql` で次の開発用アカウントを投入します。

| username | password | role | 利用範囲 |
| --- | --- | --- | --- |
| `user` | `userpass` | USER | Bugの作成・参照・更新 |
| `admin` | `adminpass` | ADMIN | Bugの作成・参照・更新・削除 |

これらはローカル開発・動作確認専用です。本番用の認証情報として使用しません。DBには平文passwordではなくBCrypt hashを保存します。

## テスト

DB結合テストでは、開発用DBとは別の `bug_tracker_test` を使用します。

テストDBが未作成の場合は初回のみ作成します。

```powershell
docker exec -it bug-tracker-postgres `
  psql -U bug_user -d postgres `
  -c "CREATE DATABASE bug_tracker_test OWNER bug_user;"
```

全テスト：

```powershell
.\mvnw.cmd test
```

設定、ApplicationContext、Securityを変更した直後は次を使用します。

```powershell
.\mvnw.cmd clean test
```

期待結果：

```text
BUILD SUCCESS
```

テストの境界と保証内容は [テスト設計](docs/test-design.md) を参照してください。

## CI

GitHub Actionsで、`push` / `pull_request` 時にMavenテストを自動実行します。

CIの設定は `.github/workflows/ci.yml`、失敗時の確認手順は [Runbook](docs/operations.md) を参照してください。

## ドキュメント

| ドキュメント | 役割 |
| --- | --- |
| [requirements.md](docs/requirements.md) | 要件・スコープ・Security要件 |
| [api-spec.md](docs/api-spec.md) | API Request / Response / HTTP契約 |
| [data-model.md](docs/data-model.md) | DBモデル・JPA対応・schema方針 |
| [decisions.md](docs/decisions.md) | 採用した設計判断と理由 |
| [test-design.md](docs/test-design.md) | 自動テストの境界・保証観点 |
| [operations.md](docs/operations.md) | 起動・操作・DB確認・テスト・障害対応のRunbook |

## 現在の主な制約

- Bug CRUD専用の画面は未実装で、現段階の操作確認はAPI / `scripts/smoke.ps1` を使用する
- JUnitによるController / Service / Repository / PostgreSQLを実HTTPで一貫して通すE2E自動テストは未実装
- GET / PUT / DELETEのController HTTP境界テストは、必要性に応じて拡張する
- 一覧検索は `status` / `priority` / `keyword` を対象とし、複雑な検索条件や高度な検索最適化は対象外

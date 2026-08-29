# Bug Tracker（不具合管理アプリ）

Spring BootとPostgreSQLで構築した、不具合（Bug）の登録・検索・参照・更新・削除を行うWebアプリケーションです。

Controller / Service / Repositoryを分離し、Bean Validation、統一エラーレスポンス、PostgreSQL永続化、自動テスト、GitHub ActionsによるCIを備えています。

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
- 統一エラーレスポンス
- Service単体テスト、DB結合テスト、Controller HTTP境界テスト
- GitHub Actionsによる自動テスト

APIの詳細なRequest / Response / HTTPステータス / エラー契約は [API仕様](docs/api-spec.md) を参照してください。

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

### 2. 開発用schemaを作成する

初回セットアップ時に `bugs` と `users` のschemaを `bug_tracker` へ適用します。

```powershell
Get-Content .\docs\db\bugs.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker

Get-Content .\docs\db\users.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

devプロファイルでは ddl-auto=validate を使用し、JPA Entityと既存schemaの整合を確認します。

### 3. アプリを起動する

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 4. 疎通確認

```powershell
curl.exe "http://localhost:8080/health"
```

期待結果：

```text
OK
```

詳細なDB確認やトラブルシュートは [Runbook](docs/operations.md) を参照してください。

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

期待結果：

```text
BUILD SUCCESS
```

テストの境界と保証内容は [テスト設計](docs/test-design.md)、設定・障害切り分けは [Runbook](docs/operations.md) を参照してください。

## CI

GitHub Actionsで、`push` / `pull_request` 時にMavenテストを自動実行します。

ローカルとCIで同じテストを実行し、変更による回帰を確認します。CIの具体的な設定は `.github/workflows/ci.yml` を参照してください。

## ドキュメント

| ドキュメント                            | 内容                                 |
| --------------------------------------- | ------------------------------------ |
| [requirements.md](docs/requirements.md) | 要件・スコープ・Security要件         |
| [api-spec.md](docs/api-spec.md)         | API・エラー・SecurityのHTTP契約      |
| [data-model.md](docs/data-model.md)     | DBモデル・JPA対応・schema方針        |
| [decisions.md](docs/decisions.md)       | 設計判断と理由（Security方針を含む） |
| [test-design.md](docs/test-design.md)   | テスト境界・保証観点                 |
| [operations.md](docs/operations.md)     | 起動・DB・テスト・CI障害時のRunbook  |

Securityの詳細は、要件を `requirements.md`、HTTP上の挙動を `api-spec.md`、採用理由を `decisions.md` で管理します。

## 現在の主な制約

- Spring Securityは導入済み。Bug APIは認証必須で、DELETEはADMINのみ許可する。
- HTTPからController / Service / Repository / PostgreSQLをすべて実物で通すE2E相当テストは未実装
- GET / PUT / DELETEのController HTTP境界テストは今後必要性に応じて拡張する
- 一覧検索は `status` / `priority` / `keyword` を対象とし、複雑な検索条件や高度な検索最適化は対象外

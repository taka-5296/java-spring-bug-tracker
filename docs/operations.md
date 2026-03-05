# 運用手順書（ローカル） -v0.1

## 目的

- ローカル環境での起動・疎通確認・ログ確認・DB接続確認の手順をまとめる

## アプリの起動と疎通確認

### 起動

- コマンド：
  - `./mvnw spring-boot:run`

### 疎通確認（ヘルスチェック）

- リクエスト：
  - `GET /health`
- 期待結果：
  - `200 OK`（アプリが稼働していること）

## ログの見方（最低限）

- INFO：
  - 正常系の処理（起動完了、リクエスト受信、主要処理の通過点）
- ERROR：
  - 例外が発生した場合のスタックトレースや原因メッセージ
- 方針：
  - 不具合調査は「ERRORログ」→「直前のINFOログ」の順で追う

## bugsテーブル作成と確認（psql）

### テーブル作成

- `docker exec -it bug-tracker-postgres psql -U bug_user -d bug_tracker`
- `\i docs/db/bugs.sql`

### 確認

- `\dt`
- `SELECT id, title, status, priority, created_at, updated_at FROM bugs ORDER BY id ASC;`
- `\q`

## データベース（PostgreSQL）の起動・停止

### 起動（Docker）

初回起動：

```PowerShell
docker run --name bug-tracker-postgres `
  -e POSTGRES_DB=bug_tracker `
  -e POSTGRES_USER=bug_user `
  -e POSTGRES_PASSWORD=bug_pass `
  -p 5432:5432 `
  -d postgres:16
```

再起動時：
`docker start bug-tracker-postgres`

### 停止（Docker）

`docker stop bug-tracker-postgres`

※後にcompose運用に変更

## DB接続（psql）と簡易確認

### psqlに接続

- コマンド例（コンテナ名は環境に合わせる）：
  - `docker exec -it bug-tracker-postgres psql -U bug_user -d bug_tracker`

### テーブル確認

- コマンド：
  - `\dt`

### 終了

- コマンド：
  - `\q`

## トラブルシュート（最小）

- アプリが起動しない：
  - まず `./mvnw test` が通るか確認
  - 直近のERRORログを確認
- DBに接続できない：
  - `docker ps` でPostgreSQLコンテナが起動しているか確認
  - `docker logs <container>` でエラー有無を確認
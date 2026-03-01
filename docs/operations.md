# 運用手順書（ローカル） -v0

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

## データベース（PostgreSQL）の起動・停止
### 起動（Docker）
- コマンド：
  - `docker compose up -d`

### 停止（Docker）
- コマンド：
  - `docker compose down`

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
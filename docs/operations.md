# Bug Tracker Runbook

## 1. 目的

ローカル環境でBug Trackerを起動し、主要機能を操作・確認し、DB・テスト・CI・障害発生時の初動切り分けまで再現できる手順を定義する。

文書の役割は次のとおりとする。

- README：初回起動と確認の入口
- `api-spec.md`：Request / Response / HTTPステータスなどのAPI契約
- 本書：実際の起動、操作確認、DB確認、障害対応
- `test-design.md`：自動テストの境界と保証内容

本書ではAPI仕様を重複して定義せず、実行に必要な代表操作と期待結果を記載する。

## 2. 前提と設定

### 2.1 前提

- Java 17
- Docker Desktop
- Windows PowerShell
- リポジトリルートでコマンドを実行する

### 2.2 設定ファイル

```text
src/main/resources/
├─ application.properties
└─ application-dev.properties

src/test/resources/
└─ application-test.properties
```

### 2.3 dev / testの使い分け

| profile | DB | ddl-auto | 用途 |
| --- | --- | --- | --- |
| dev | `bug_tracker` | `validate` | ローカル起動・操作確認 |
| test | `bug_tracker_test` | `create-drop` | 自動テスト |

devでは `docs/db/bugs.sql` / `docs/db/users.sql` でschemaを管理し、データを保持する。testではSQL初期化を行わず、テスト起動時にschemaを作成・破棄する。

## 3. PostgreSQLの準備

### 3.1 初回コンテナ作成

```powershell
docker run --name bug-tracker-postgres `
  -e POSTGRES_DB=bug_tracker `
  -e POSTGRES_USER=bug_user `
  -e POSTGRES_PASSWORD=bug_pass `
  -p 5432:5432 `
  -d postgres:16
```

### 3.2 2回目以降の起動

```powershell
docker start bug-tracker-postgres
```

起動確認：

```powershell
docker ps --filter "name=bug-tracker-postgres"
```

### 3.3 dev schemaと初期ユーザーを適用

```powershell
Get-Content .\docs\db\bugs.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker

Get-Content .\docs\db\users.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

`ddl-auto=validate` により、Entityと既存schemaの不整合はアプリ起動時に検出する。

## 4. アプリケーションの起動と停止

### 4.1 起動

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

起動ログにエラーがなく、8080番ポートで待受状態になることを確認する。

### 4.2 公開経路の疎通確認

別のPowerShellで実行する。

```powershell
curl.exe -i "http://localhost:8080/health"
```

期待結果：

```text
HTTP/1.1 200
...

OK
```

### 4.3 停止

アプリを起動しているPowerShellで `Ctrl + C` を押す。

PostgreSQLも停止する場合：

```powershell
docker stop bug-tracker-postgres
```

## 5. 基本操作

現段階ではBug CRUD専用の画面は未実装である。主要操作はAPIまたは `scripts/smoke.ps1` で確認する。

### 5.1 利用できる操作

| 操作 | Method | Path | USER | ADMIN | 主な成功結果 |
| --- | --- | --- | --- | --- | --- |
| Bug作成 | POST | `/api/bugs` | 可 | 可 | 201 |
| Bug一覧 | GET | `/api/bugs` | 可 | 可 | 200 |
| Bug個別取得 | GET | `/api/bugs/{id}` | 可 | 可 | 200 |
| Bug更新 | PUT | `/api/bugs/{id}` | 可 | 可 | 200 |
| Bug削除 | DELETE | `/api/bugs/{id}` | 不可 | 可 | 204 |

Request / Responseの正確な項目定義は `api-spec.md` を参照する。

### 5.2 一括スモーク確認

アプリ起動中に次を実行する。

```powershell
.\scripts\smoke.ps1
```

スクリプトはローカル開発用USER / ADMINでSessionとCSRF tokenを処理し、次を順に確認する。

1. `/health` が未認証で200
2. 未認証 `/api/bugs` がログイン画面へ302
3. DBのUSER / ADMINでログイン
4. USERでBug一覧取得が200
5. ADMINでBug作成が201
6. 作成したBugの個別取得が200
7. Bug更新が200
8. `status` / `priority` / `keyword` 検索が200
9. `page` / `size` ページングが200
10. USERによるDELETEが403
11. ADMINによるDELETEが204
12. 削除後のGETが404

最後に次が表示されれば一括確認は成功とする。

```text
[SUCCESS] Smoke check completed.
```

Cookie、Session、CSRF tokenの取得処理は `scripts/smoke.ps1` に集約し、Runbookへ長大なcurl手順を重複させない。

### 5.3 Bug作成

代表Request：

```json
{
  "title": "login error",
  "description": "login fails after submit",
  "status": "OPEN",
  "priority": "HIGH"
}
```

操作：

```text
POST /api/bugs
```

期待結果：

```text
201 Created
Location: {baseUrl}/api/bugs/{id}
```

### 5.4 一覧・個別取得

一覧：

```text
GET /api/bugs
```

個別：

```text
GET /api/bugs/{id}
```

期待結果はいずれも正常時200。存在しないIDの個別取得は404。

### 5.5 Bug更新

代表Request：

```json
{
  "title": "login error fixed",
  "description": "verified after update",
  "status": "DONE",
  "priority": "MEDIUM"
}
```

操作：

```text
PUT /api/bugs/{id}
```

期待結果：

```text
200 OK
```

### 5.6 検索

例：

```text
GET /api/bugs?status=OPEN&priority=HIGH&keyword=login&page=0&size=10
```

`status` / `priority` / `keyword` を複数指定した場合はAND条件で検索する。

### 5.7 ページング

例：

```text
GET /api/bugs?page=0&size=5
```

一覧レスポンスは `items` と `meta` を返す。詳細は `api-spec.md` を参照する。

### 5.8 Bug削除

```text
DELETE /api/bugs/{id}
```

- USER：403 Forbidden
- ADMIN：204 No Content
- 削除後に同じIDをGET：404 Not Found

## 6. 認証・認可の確認

### 6.1 ローカル開発用アカウント

`docs/db/users.sql` で次を投入する。

| username | password | role | 利用範囲 |
| --- | --- | --- | --- |
| `user` | `userpass` | USER | 作成・参照・更新 |
| `admin` | `adminpass` | ADMIN | 作成・参照・更新・削除 |

これらはローカル開発・動作確認専用であり、本番用途では使用しない。

DBには平文passwordではなくBCrypt hashを保存する。

### 6.2 未認証アクセス

```powershell
curl.exe -i "http://localhost:8080/api/bugs"
```

期待結果：

```text
HTTP/1.1 302
Location: http://localhost:8080/login
```

### 6.3 USER / ADMIN / CSRF

Session認証とCSRFを含む代表確認は `scripts/smoke.ps1` で実行する。

確認基準：

- DBのUSER / ADMINでログインできる
- USERは作成・参照・更新を利用できる
- USERは有効なCSRF tokenがあってもDELETEを実行できず403になる
- ADMINは有効なCSRF token付きでDELETEを実行できる
- CSRF tokenがない更新系リクエストは403になる

認証・認可のHTTP契約は `api-spec.md`、自動テストの保証範囲は `test-design.md` を参照する。

## 7. DB確認

### 7.1 users

```powershell
docker exec -it bug-tracker-postgres `
  psql -U bug_user -d bug_tracker `
  -c "SELECT username, role, enabled, left(password_hash, 4) AS hash_prefix FROM users ORDER BY username;"
```

期待する要点：

```text
username | role  | enabled | hash_prefix
---------+-------+---------+------------
admin    | ADMIN | t       | $2a$
user     | USER  | t       | $2a$
```

`password_hash` に平文passwordが保存されていないことを確認する。

### 7.2 bugs

```powershell
docker exec -it bug-tracker-postgres `
  psql -U bug_user -d bug_tracker `
  -c "SELECT id, title, status, priority, created_at, updated_at FROM bugs ORDER BY id DESC LIMIT 10;"
```

API操作後のデータがPostgreSQLへ永続化されているか確認する際に使用する。

## 8. test DBと自動テスト

### 8.1 test DB作成

初回のみ実行する。

```powershell
docker exec -it bug-tracker-postgres `
  psql -U bug_user -d postgres `
  -c "CREATE DATABASE bug_tracker_test OWNER bug_user;"
```

存在確認：

```powershell
docker exec -it bug-tracker-postgres `
  psql -U bug_user -d postgres `
  -c "\l"
```

### 8.2 テスト実行

通常：

```powershell
.\mvnw.cmd test
```

設定、ApplicationContext、Security等を変更した直後：

```powershell
.\mvnw.cmd clean test
```

ログをファイルへ保存する場合：

```powershell
.\mvnw.cmd clean test 2>&1 |
Tee-Object -FilePath .\mvn-test-log-current.txt
```

期待結果：

```text
BUILD SUCCESS
```

## 9. 障害切り分け

### 9.1 アプリが起動しない

確認順：

1. PostgreSQLコンテナがUpか
2. `bug_tracker` が存在するか
3. `bugs` / `users` schemaを適用済みか
4. `application-dev.properties` のDB接続情報が一致しているか
5. 起動ログの最初の `Caused by:` を確認する

### 9.2 DB接続失敗

```powershell
docker ps --filter "name=bug-tracker-postgres"
```

```powershell
docker logs bug-tracker-postgres
```

```powershell
docker exec -it bug-tracker-postgres psql -U bug_user -d postgres -c "\l"
```

確認項目：

- コンテナがUpか
- DB名が一致しているか
- username / password / portが一致しているか
- dev / testを取り違えていないか

### 9.3 認証できない

確認順：

1. `users` テーブルに対象usernameが存在するか
2. `enabled=true` か
3. roleが `USER` または `ADMIN` か
4. `password_hash` がBCrypt hashか
5. devプロファイルで `bug_tracker` に接続しているか

### 9.4 更新系リクエストが403になる

POST / PUT / DELETEでは、認証済みSessionに加えて有効なCSRF tokenが必要である。

権限不足とCSRF不足を区別するため、まず `scripts/smoke.ps1` または `SecurityIntegrationTest` の結果と比較する。

### 9.5 テスト失敗

1. `Tests run / Failures / Errors` を確認
2. 失敗したテストクラス・メソッドを確認
3. 最初の例外メッセージを確認
4. `Caused by:` を最も深い原因まで追う
5. DB接続URL、active profile、ApplicationContext設定を確認
6. package名やBean名を手掛かりに修正対象を絞る

Spring内部のstack traceを最初から全行読む必要はない。

## 10. CI失敗時

まずローカルで同じテストを再現する。

```powershell
.\mvnw.cmd clean test
```

次に以下を比較する。

- Java 17
- Maven Wrapper
- PostgreSQL version
- DB名
- user/password
- port
- `SPRING_PROFILES_ACTIVE=test`
- `.github/workflows/ci.yml` のPostgreSQL service設定

ローカルが失敗している状態でCIだけを先に修正しない。

## 11. ログ

- INFO：正常な主要処理・想定内エラー
- WARN：継続可能だが注意すべき状態
- ERROR：想定外障害。stack traceを残す

request body全文、password、Session IDなどの認証情報を不用意にログへ出力しない。

500レスポンスでは内部例外の詳細をクライアントへそのまま公開しない。

## 12. 障害報告テンプレート

- 発生事象:
- 影響範囲:
- 再現手順:
- 期待結果:
- 実際の結果:
- 直近変更:
- ログの根本例外:
- 試したこと:
- 暫定対応:
- 次の確認:

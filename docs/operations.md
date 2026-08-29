# Bug Tracker Runbook

## 1. 目的

ローカル環境での起動・Security確認・テスト・DB確認と、テスト / CI失敗時の初動切り分けを定義する。

通常の機能説明とAPI契約はREADME / `api-spec.md` を参照する。

## 2. 設定ファイル

```text
src/main/resources/
├─ application.properties
└─ application-dev.properties

src/test/resources/
└─ application-test.properties
```

ファイル名はSpring Bootの規約どおり `application-{profile}.properties` とする。

### common

`application.properties`

```properties
spring.application.name=bug-tracker
```

### dev

- DB: `bug_tracker`
- `ddl-auto=validate`
- schemaは `docs/db/bugs.sql` / `docs/db/users.sql` で管理
- データを保持する

### test

- DB: `bug_tracker_test`
- `ddl-auto=create-drop`
- SQL初期化は行わない
- 自動テスト専用

## 3. PostgreSQL

### 初回コンテナ作成

```powershell
docker run --name bug-tracker-postgres `
  -e POSTGRES_DB=bug_tracker `
  -e POSTGRES_USER=bug_user `
  -e POSTGRES_PASSWORD=bug_pass `
  -p 5432:5432 `
  -d postgres:16
```

### 2回目以降

```powershell
docker start bug-tracker-postgres
```

### 起動確認

```powershell
docker ps --filter "name=bug-tracker-postgres"
```

## 4. dev schema

dev DBへschemaを適用する。

```powershell
Get-Content .\docs\db\bugs.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker

Get-Content .\docs\db\users.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

`application-dev.properties` の `ddl-auto=validate` により、Entityと既存schemaの不整合を起動時に検出する。

## 5. Security手動確認

### 5.1 目的

未ログイン、USER、ADMINの3つのアクセス状態について、DB認証、認可、CSRFをローカル環境で再現する。

初期ユーザーは `docs/db/users.sql` で投入する。固定アカウントはローカル開発・動作確認専用であり、本番用途では使用しない。

| 区分 | username | password | role |
| --- | --- | --- | --- |
| USER | `user` | `userpass` | USER |
| ADMIN | `admin` | `adminpass` | ADMIN |

DBには平文passwordではなくBCrypt hashを保存する。

### 5.2 前提確認

PostgreSQLを起動し、schemaを適用する。

```powershell
docker start bug-tracker-postgres

Get-Content .\docs\db\bugs.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker

Get-Content .\docs\db\users.sql |
docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

DB上の認証ユーザーを確認する。

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

### 5.3 アプリ起動

別のPowerShellでdevプロファイルを指定して起動する。

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

起動ログにエラーがなく、8080番ポートで待受状態になることを確認する。

### 5.4 未ログインで公開経路を確認

```powershell
curl.exe -i "http://localhost:8080/health"
```

期待結果：

```text
HTTP/1.1 200
...

OK
```

### 5.5 未ログインで保護APIを確認

```powershell
curl.exe -i "http://localhost:8080/api/bugs"
```

期待結果：

```text
HTTP/1.1 302
Location: http://localhost:8080/login
```

未認証状態ではBug APIへ到達せず、フォームログインへリダイレクトされる。

### 5.6 USERでDBログインする

まずログインページを取得し、Session cookieとCSRF tokenを保存する。

```powershell
curl.exe -s `
  -c .\user-cookies.txt `
  "http://localhost:8080/login" `
  -o .\user-login.html

$userLoginHtml = Get-Content .\user-login.html -Raw

if ($userLoginHtml -match 'name="_csrf"[^>]*value="([^"]+)"') {
    $userCsrf = $Matches[1]
} else {
    throw "CSRF token was not found in login page."
}
```

DBユーザー `user` でログインする。

```powershell
curl.exe -i `
  -b .\user-cookies.txt `
  -c .\user-cookies.txt `
  -X POST "http://localhost:8080/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=user" `
  --data-urlencode "password=userpass" `
  --data-urlencode "_csrf=$userCsrf"
```

期待結果は302であり、認証済みSessionが `user-cookies.txt` に保存される。

認証済みUSERでBug一覧を取得する。

```powershell
curl.exe -i `
  -b .\user-cookies.txt `
  "http://localhost:8080/api/bugs"
```

期待結果：

```text
HTTP/1.1 200
```

これにより、DBから取得したUSERで保護APIを利用できることを確認する。

### 5.7 USERのDELETE拒否を確認

ログイン後のCSRF tokenを再取得する。

```powershell
curl.exe -s `
  -b .\user-cookies.txt `
  -c .\user-cookies.txt `
  "http://localhost:8080/login" `
  -o .\user-login-authenticated.html

$userLoginHtml = Get-Content .\user-login-authenticated.html -Raw

if ($userLoginHtml -match 'name="_csrf"[^>]*value="([^"]+)"') {
    $userCsrf = $Matches[1]
} else {
    throw "CSRF token was not found after USER login."
}
```

有効なCSRF tokenを付けてDELETEする。

```powershell
curl.exe -i `
  -b .\user-cookies.txt `
  -X DELETE "http://localhost:8080/api/bugs/1" `
  -H "X-CSRF-TOKEN: $userCsrf"
```

期待結果：

```text
HTTP/1.1 403
```

CSRF tokenは有効でも、USERにはDELETE権限がないため認可で拒否される。

### 5.8 ADMINでDBログインする

ADMIN用のSession cookieとログイン用CSRF tokenを取得する。

```powershell
curl.exe -s `
  -c .\admin-cookies.txt `
  "http://localhost:8080/login" `
  -o .\admin-login.html

$adminLoginHtml = Get-Content .\admin-login.html -Raw

if ($adminLoginHtml -match 'name="_csrf"[^>]*value="([^"]+)"') {
    $adminCsrf = $Matches[1]
} else {
    throw "CSRF token was not found in login page."
}
```

DBユーザー `admin` でログインする。

```powershell
curl.exe -i `
  -b .\admin-cookies.txt `
  -c .\admin-cookies.txt `
  -X POST "http://localhost:8080/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "username=admin" `
  --data-urlencode "password=adminpass" `
  --data-urlencode "_csrf=$adminCsrf"
```

期待結果は302であり、認証済みSessionが `admin-cookies.txt` に保存される。

ログイン後のCSRF tokenを再取得する。

```powershell
curl.exe -s `
  -b .\admin-cookies.txt `
  -c .\admin-cookies.txt `
  "http://localhost:8080/login" `
  -o .\admin-login-authenticated.html

$adminLoginHtml = Get-Content .\admin-login-authenticated.html -Raw

if ($adminLoginHtml -match 'name="_csrf"[^>]*value="([^"]+)"') {
    $adminCsrf = $Matches[1]
} else {
    throw "CSRF token was not found after ADMIN login."
}
```

### 5.9 ADMINでBugを作成してDELETEする

削除確認専用のBugを作成する。

```powershell
$adminCreateBody = '{"title":"security verification bug","description":"created by Runbook","status":"OPEN","priority":"LOW"}'

curl.exe -s `
  -D .\admin-create-headers.txt `
  -o .\admin-create-body.json `
  -b .\admin-cookies.txt `
  -X POST "http://localhost:8080/api/bugs" `
  -H "Content-Type: application/json" `
  -H "X-CSRF-TOKEN: $adminCsrf" `
  --data-raw $adminCreateBody

Get-Content .\admin-create-headers.txt
```

期待結果：

```text
HTTP/1.1 201
Location: http://localhost:8080/api/bugs/{id}
```

作成されたBugのURLを取得する。

```powershell
$locationMatch = Select-String `
  -Path .\admin-create-headers.txt `
  -Pattern '^Location:\s*(.+)$'

if (-not $locationMatch) {
    throw "Location header was not found."
}

$bugLocation = $locationMatch.Matches[0].Groups[1].Value.Trim()

if ($bugLocation -match '^https?://') {
    $bugUrl = $bugLocation
} else {
    $bugUrl = "http://localhost:8080$bugLocation"
}

$bugUrl
```

ADMINで同じBugをDELETEする。

```powershell
curl.exe -i `
  -b .\admin-cookies.txt `
  -X DELETE $bugUrl `
  -H "X-CSRF-TOKEN: $adminCsrf"
```

期待結果：

```text
HTTP/1.1 204
```

これにより、ADMINではDELETEが認可されることを確認する。

### 5.10 Security確認結果

以下がすべて成立すれば、未ログイン・USER・ADMINとDB認証のローカル再現確認は完了とする。

- 未ログインで `/health` が200
- 未ログインで `/api/bugs` が `/login` へ302
- `user` / `userpass` でDBログインできる
- USERで `/api/bugs` を利用できる
- USER + 有効CSRF tokenでもDELETEは403
- `admin` / `adminpass` でDBログインできる
- ADMIN + 有効CSRF tokenでBug作成が201
- ADMIN + 有効CSRF tokenでDELETEが204
- usersテーブルのpasswordがBCrypt hashで保存されている

確認後、作成した一時ファイルを削除する。

```powershell
Remove-Item `
  .\user-cookies.txt, `
  .\user-login.html, `
  .\user-login-authenticated.html, `
  .\admin-cookies.txt, `
  .\admin-login.html, `
  .\admin-login-authenticated.html, `
  .\admin-create-headers.txt, `
  .\admin-create-body.json `
  -ErrorAction SilentlyContinue
```

## 6. test DB

テストDBは初回のみ作成する。

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

`bug_tracker_test` 内のテーブルはHibernateの `create-drop` で作成・破棄する。

## 7. ローカルテスト

通常：

```powershell
.\mvnw.cmd test
```

設定、ApplicationContext、Security等を変更した直後：

```powershell
.\mvnw.cmd clean test
```

ログ保存：

```powershell
.\mvnw.cmd clean test 2>&1 |
Tee-Object -FilePath .\mvn-test-log-current.txt
```

## 8. テスト失敗時の調査順

1. `Tests run / Failures / Errors` を確認
2. 失敗したテストクラス・メソッドを確認
3. 最初の例外メッセージを確認
4. `Caused by:` を最も深い原因まで追う
5. DB接続URL、active profile、ApplicationContext設定を確認
6. 自分のpackage名やBean名を手掛かりに修正対象を絞る

Spring内部のstack traceを最初から全行読む必要はない。

## 9. DB接続失敗の切り分け

確認順：

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
- `bug_tracker_test` が存在するか
- username / password / portが一致しているか
- test profileが有効か

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
- workflowのservice設定

ローカルが失敗している状態でCIだけを先に修正しない。

## 11. ログレベル

- INFO: 正常な主要処理・想定内エラー
- WARN: 継続可能だが注意すべき状態
- ERROR: 想定外障害。stack traceを残す

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
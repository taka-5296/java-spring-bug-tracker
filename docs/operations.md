# Bug Tracker Runbook

## 1. 目的

ローカル環境での起動・テスト・DB確認と、テスト / CI失敗時の初動切り分けを定義する。

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

## 5. test DB

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

## 6. ローカルテスト

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

## 7. テスト失敗時の調査順

1. `Tests run / Failures / Errors` を確認
2. 失敗したテストクラス・メソッドを確認
3. 最初の例外メッセージを確認
4. `Caused by:` を最も深い原因まで追う
5. DB接続URL、active profile、ApplicationContext設定を確認
6. 自分のpackage名やBean名を手掛かりに修正対象を絞る

Spring内部のstack traceを最初から全行読む必要はない。

## 8. DB接続失敗の切り分け

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

## 9. CI失敗時

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

## 10. ログレベル

- INFO: 正常な主要処理・想定内エラー
- WARN: 継続可能だが注意すべき状態
- ERROR: 想定外障害。stack traceを残す

500レスポンスでは内部例外の詳細をクライアントへそのまま公開しない。

## 11. 障害報告テンプレート

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

# Bug Tracker（不具合管理アプリ）

## 概要

本システムは、Spring BootとPostgreSQLを用いたシンプルな不具合管理Webアプリです。

## まずやること（最短）

1. 起動

```PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

→ 起動状態になる

2. 疎通確認

```PowerShell
GET http://localhost:8080/health
```

 → "OK"が出力

## 主要リンク

- [ローカル起動手順](#ローカル起動手順)
- [動作確認](#動作確認)
- [運用ルール（Git/GitHub）](#運用ルールgitgithub)
- [API仕様](docs/api-spec-v0.md)
- [エラー仕様](docs/error-spec-v0.md)
- [ER図 v0.1](docs/er-v0.md)
- [運用手順](docs/operations.md)
- [PRテンプレ](.github/PULL_REQUEST_TEMPLATE.md)

## 主な機能

### 現時点

- ヘルスチェック：`GET /health`（OKを返す）
- Bug（チケット）のCRUD API（DB永続化）
  - 作成：`POST /api/bugs`
  - 一覧：`GET /api/bugs`
    - `status` クエリパラメータ指定でステータス絞り込み可能（例：`GET /api/bugs?status=OPEN`）
    - `priority` クエリパラメータ指定で重要度絞り込み可能（例：`GET /api/bugs?priority=HIGH`）
    - `keyword` クエリパラメータ指定で `title` または `description` の部分一致検索が可能（例：`GET /api/bugs?keyword=test`）
    - 複数条件指定時は AND 条件で絞り込み可能（例：`GET /api/bugs?status=OPEN&priority=HIGH&keyword=test`）
    - `page` / `size` クエリパラメータ指定でページング可能（例：`GET /api/bugs?page=0&size=10`）
    - 一覧レスポンスは `items + meta` 形式で返却

  - 個別：`GET /api/bugs/{id}`
  - 更新：`PUT /api/bugs/{id}`
  - 削除：`DELETE /api/bugs/{id}`
  - 入力バリデーション（400）：title必須（VALIDATION_ERROR）
  - 不正JSON（400）：壊れたJSONまたは不正値（INVALID_JSON）
  - 存在しないID（404）：NOT_FOUND（GET/PUT/DELETEで共通）
  - status/priority未指定時は Service 側で OPEN/LOW をデフォルト補完
- DB：PostgreSQL（Docker） + JPA（ORM）

### 予定

- ステータス管理：Open / In Progress / Done
- 認証・権限：USER / ADMIN

## 技術スタック

### 現時点

- Java 17
- Spring Boot（Web）
- Maven Wrapper（mvnw）
- テスト
  - JUnit 5
  - Mockito
  - Service単体テスト
    - create 正常系：status / priority 未指定時に OPEN / LOW をデフォルト補完
    - NotFound 異常系：findById / updateById / deleteById でBugNotFoundException
- CI
  - GitHub Actions
  - `push` / `pull_request` を契機に `mvn test` を自動実行
- DB
  - 永続化：PostgreSQL
  - 実装方式：JPA（ORM）
  - テーブル最小案：bugs（id, title, description, status, priority, createdAt, updatedAt）

### 予定

- Thymeleaf（画面表示）
- Docker Compose
- DB を含む最小結合テスト
- HTTP レベルの確認テスト

## ローカル起動手順

### 前提

- Java 17
- Maven Wrapper（mvnw）

## ローカル起動手順（dev）

### 前提

- Java 17
- Docker Desktop（PostgreSQLをDockerで動かす場合）

### 1) PostgreSQL起動（Docker）

```
docker run --name bug-tracker-postgres `
  -e POSTGRES_DB=bug_tracker `
  -e POSTGRES_USER=bug_user `
  -e POSTGRES_PASSWORD=bug_pass `
  -p 5432:5432 `
  -d postgres:16
```

### 2) コンテナ起動確認

```PowerShell
docker ps --filter "name=bug-tracker-postgres"
```

- 期待結果
"bug-tracker-postgres"の情報が表示され、STATUS が "Up ..." になっている。

### 3) DB疎通確認（任意）

```PowerShell
docker exec -it bug-tracker-postgres psql -U bug_user -d bug_tracker
```

#### psql内で

`SELECT 1;`

`SELECT version();`

※SELECTコマンドは`;`忘れに注意

`\q`

### 4) アプリ起動（devプロファイル）

```PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

※ "-Dspring-boot.run.profiles=dev" は必ず "" で囲む（PowerShell対策）

### 5) 停止

- アプリ停止：Ctrl + C
- DB一時停止（任意）：`docker stop bug-tracker-postgres`
- DB  再起動（任意）：`docker start bug-tracker-postgres`

## API（暫定）

### エンドポイント一覧

- POST `/api/bugs`
  - Bugを作成する
- GET `/api/bugs`
  - Bug一覧を取得する
  - 任意クエリ：
    - `status`（`OPEN` / `IN_PROGRESS` / `DONE`）
    - `priority`（`LOW` / `MEDIUM` / `HIGH`）
    - `keyword`（`title` または `description` の部分一致）
    - `page`（0始まりのページ番号）
    - `size`（1ページ件数）
  - 複数条件指定時は AND 条件で検索
  - 成功レスポンスは `items + meta`
- GET `/api/bugs/{id}`
  - Bugを1件取得する
- PUT `/api/bugs/{id}`
  - Bugを更新する
- DELETE `/api/bugs/{id}`
  - Bugを削除する

### 検索・ページング例

- `GET /api/bugs?keyword=test`
- `GET /api/bugs?status=OPEN&priority=HIGH`
- `GET /api/bugs?status=OPEN&priority=HIGH&keyword=test`
- `GET /api/bugs?status=IN_PROGRESS&priority=LOW&keyword=test&page=0&size=5`

## エラー形式

- 本APIのエラーレスポンスは、以下の統一形式で返す。

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力値が不正です",
  "details": [
    "titleは必須です"
  ]
}
```

### エラーコード

- VALIDATION_ERROR : DTO Validation 失敗（400）
- INVALID_JSON     : JSON形式不正 / enum不正（400）
- NOT_FOUND        : 指定idのBugが存在しない（404）
- INTERNAL_ERROR   : 想定外エラー（500）

### 既知制約（Limitations）

- 一覧検索は `status` / `priority` / `keyword` のみ対応
- `keyword` は `title` または `description` の部分一致検索
- ソート条件の外部指定、複雑なOR条件、検索性能最適化は未対応

### ロギングポリシー

- Controller / Service の操作開始と主要結果を INFO で記録する
- 400 / 404 は想定内エラーとして INFO で記録する
- 500 は想定外エラーとして ERROR で記録し、スタックトレースを残す
- request body 全文、個人情報、巨大ペイロード、内部例外の生メッセージはログへ出しすぎない

## テスト方針

### テストの型

- AAA（Arrange / Act / Assert）で記述する
  - Arrange: 入力値・モック・戻り値などの準備
  - Act: 対象メソッドを実行
  - Assert: 戻り値・呼び出し回数・引数の中身を検証

### 命名規約

- テストメソッド名は `(対象メソッド名) + should_...` 形式を基本とする
- 何を保証するテストか、短い名前だけで分かるようにする
- 例: `findById_should_throw_not_found`

### 観点の分け方

- 正常系: 期待した値が返る、保存処理が呼ばれる
- 異常系: 例外が送出される、未存在データで失敗する

## テスト実行

### 単体テスト実行

- 現在は BugService の単体テストを追加済み
  - create 正常系：status / priority 未指定時の OPEN / LOW 自動補完
  - findAll 正常系：keyword 正規化 + Repository.search への委譲
  - findById 正常系：既存Bugを返す
  - updateById 正常系：既存Bugの更新内容を保存する
  - deleteById 正常系：既存Bugを削除する
  - findById / updateById / deleteById の NotFound 異常系

### ローカル実行

```PowerShell
.\mvnw.cmd test
```

- 期待結果
BUILD SUCCESS が表示される。

### CI実行方針

- GitHub Actions により、`push` および `pull_request` 時に自動でテストを実行する
- Java / Maven 向けの一般的な構成として、`actions/checkout` によるソース取得と `actions/setup-java` による Java 17 環境の準備を行う
- JDK 配布は `temurin` を採用
- 現時点では、GitHub の Java + Maven 向け公式例に近い `checkout@v5 + setup-java@v5` を採用
- CI では `./mvnw test` を実行する
- PR では、ローカル実行結果に加えて CI 結果も確認証拠として扱う


## 動作確認

### ヘルスチェック

- ブラウザでアクセス：`http://localhost:8080/health`
- 期待結果：`OK` が表示される

### API (Bug作成・一覧・個別)

- 以下は PowerShellの例(Windows11想定 / curl使用)

### bugsテーブル作成と確認（psql）

#### テーブル作成

- `.\docs\db\bugs.sql`のsqlファイルを流し込み、テーブル名`bugs`を作成する

```PowerShell
Get-Content .\docs\db\bugs.sql | docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

- 期待結果
CREATE TABLE 等が表示され、エラーが出ない。<br>
※必ずPowerShellの`/bug_tracker`上で行う

#### 確認

- psql内に入る

```PowerShell
docker exec -it bug-tracker-postgres psql -U bug_user -d bug_tracker
```

- psql内で以下を実行
- `\dt`　Name = bugs というテーブルが表で確認できる
- `\q` psqlから抜ける

### Bug作成（POST）

※動作確認用のサンプルデータを5件作成する  
※以降の検索・更新・削除の確認は、この5件を前提に実施する

#### 1) OPEN / LOW

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"login error","description":"auth form validation","status":"OPEN","priority":"LOW"}'

```

#### 2) IN_PROGRESS / MEDIUM

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"search bug","description":"keyword search target","status":"IN_PROGRESS","priority":"MEDIUM"}'
```

#### 3) DONE / HIGH

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"dashboard broken","description":"ui high priority issue","status":"DONE","priority":"HIGH"}'
```

#### 4) OPEN / HIGH

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"api timeout","description":"keyword api slow","status":"OPEN","priority":"HIGH"}'
```

#### 5) IN_PROGRESS / LOW

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"test data","description":"paging keyword sample","status":"IN_PROGRESS","priority":"LOW"}'
```

- 期待結果  
  各コマンドで `HTTP/1.1 201` と `Location` が返る。　　
- メモ  
  後続の `GET / PUT / DELETE` では、ここで返った id を使用する。

### Bug一覧取得（GET）

```PowerShell
curl.exe "http://localhost:8080/api/bugs"
```

- 期待結果  
`items` にBug一覧、`meta` にページ情報を含むJSONが返る。

#### ステータス絞り込み取得（GET / status）

```PowerShell
curl.exe "http://localhost:8080/api/bugs?status=OPEN"
```

- 期待結果  
status が`OPEN`のBugのみが items に入り、meta にページ情報が返る。

#### 重要度絞り込み取得（GET / priority）

```PowerShell
curl.exe "http://localhost:8080/api/bugs?priority=HIGH"
```

- 期待結果  
priority が`HIGH`のBugのみが items に入り、meta にページ情報が返る。

#### キーワード検索（GET / keyword）

```powershell
curl.exe "http://localhost:8080/api/bugs?keyword=keyword"
```

- 期待結果  
`title` または `description` に `keyword` の文字を含むBugのみが返る。

#### 複合条件検索（GET / status, priority, keyword）

```powershell
curl.exe "http://localhost:8080/api/bugs?status=OPEN&priority=HIGH&keyword=api"
```

- 期待結果  
statusが `OPEN` / priorityが `HIGH` / titleまたはdescriptionに `api` の文字列を含んだbugが返る。

#### ページング付きBug一覧取得（GET / page,size）

```PowerShell
curl.exe "http://localhost:8080/api/bugs?page=0&size=2"
```

- 期待結果  
1ページあたり2件で items が返り、meta.page=0, meta.size=2 になる。

#### ページング付き複合条件

```powershell
curl.exe "http://localhost:8080/api/bugs?status=IN_PROGRESS&priority=LOW&keyword=sample&page=0&size=5"
```

- 期待結果  
条件に一致する結果が返り、 `meta.page=0`、 `meta.size=5` になる。

### Bug個別取得（GET）

※`http://localhost:8080/api/bugs/{id}` の `{id}` を取得したい bug の id に書き換えて利用する。  
下記の例では `id=4` とする。

```PowerShell
curl.exe -i "http://localhost:8080/api/bugs/4"
```

- 期待結果（GET）  
`"HTTP/1.1 200"` と、{id}で指定したBugのJSONが返る。

### Bug更新（PUT）

※`http://localhost:8080/api/bugs/{id}` の `{id}` を更新したい bug の id に書き換えて利用する。  
 `id=4` とする。

#### 例： id = 4 の bug の場合

```PowerShell
curl.exe -i -X PUT "http://localhost:8080/api/bugs/4" -H "Content-Type: application/json" --data-raw '{"title":"api timeout fixed","description":"updated by curl","status":"DONE","priority":"HIGH"}'
```

- 期待結果  
`HTTP/1.1 200` と、更新後のid=4のBugのJSONが返る。

### Bug削除（DELETE）

```PowerShell
curl.exe -i -X DELETE "http://localhost:8080/api/bugs/4"
```

- 期待結果  
`HTTP/1.1 204`（No Content）が返る。

#### 削除後の確認（GET → 404）

```PowerShell
curl.exe -i "http://localhost:8080/api/bugs/4"
```

- 期待結果  
`HTTP/1.1 404` と `"code":"NOT_FOUND"`のエラーJSONが返る

### Validation / エラー確認（400）

#### 作成時のValidationエラー（POST）

※titleが空 `"title":"",`

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"","description":"created by curl"}'
```

- 期待結果  
`HTTP/1.1 400`
`"code":"VALIDATION_ERROR"` のエラーJSONが返る
details に `title must not be blank` が含まれる

#### 更新時のValidationエラー（PUT）

※`http://localhost:8080/api/bugs/{id}` の `{id}` を更新したい bug の id に書き換えて利用する。  
 下記では `id = 5` とする。

※titleが空 `"title":"",`

```PowerShell
curl.exe -i -X PUT "http://localhost:8080/api/bugs/5" -H "Content-Type: application/json" --data-raw '{"title":"","description":"updated by curl","status":"DONE","priority":"HIGH"}'
```

※ `{id}` は文字列のまま使わず、実際に作成された数値idへ置き換えること

- 期待結果  
`HTTP/1.1 400`
`"code":"VALIDATION_ERROR"` のエラーJSONが返る

#### 不正JSON / enum不正（PUT）

※`http://localhost:8080/api/bugs/{id}` の `{id}` を更新したい bug の id に書き換えて利用する。  
 下記では `id = 5` とする。

※ 下記例ではstatusが空 `""` （enum不正）

```PowerShell
curl.exe -i -X PUT "http://localhost:8080/api/bugs/5" -H "Content-Type: application/json" --data-raw '{"title":"updated title","description":"updated by curl","status":"","priority":"HIGH"}'
```

- 期待結果  
`HTTP/1.1 400` と `"code":"INVALID_JSON"` のエラーJSONが返る

## 運用ルール（Git/GitHub）

- ブランチ運用
  - `main` + `feature/*`
  - 原則として `main` へ直接 push しない
  - `feature/*` で作業し、PR 経由で `main` へ merge する

- CI 運用
  - `push` / `pull_request` 時に GitHub Actions で自動テストを実行する
  - PR には、少なくとも以下の確認証拠を記載する
    - `.\mvnw.cmd test` の実行
    - CI 結果
    - 必要に応じて curl / DB 確認結果

- コミット粒度：意味のある単位。
    feat: 新しい機能
    fix: バグの修正
    docs: ドキュメントのみの変更
    style: 空白、フォーマット、セミコロン追加など
    refactor: 仕様に影響がないコード改善(リファクタ)
    perf: パフォーマンス向上関連
    test: テスト関連
    chore: ビルド、補助ツール、ライブラリ関連
    wip:~(no PR): 途中保存、PRなし
- README：毎日「今日の変更点」に1〜3行追記

## 開発ログ（要約）

- 詳細な日次ログは `docs/logs/daily-log.md`（または管理用ログファイル）を参照

### Week1

- Spring Boot の初期セットアップを実施
- `/health` と Bug 最小API（作成 / 一覧 / 個別）を追加
- Validation / 統一エラー形式 / PR・Issue テンプレを整備

### Week2

- PostgreSQL + JPA による永続化へ移行
- Bug の CRUD API を完成
- API / エラー / ER / スキーマ / 運用 docs を整備

### Week3

- 一覧検索（status / priority / keyword）とページングを追加
- Validation / 例外 / ログ方針を整理
- README の再現手順を更新して動作確認導線を固定

### Week4

- BugService の単体テストを追加
- 正常系 / NotFound 異常系の主要観点を確認
- GitHub Actions による CI を導入し、`push` / `pull_request` で `mvn test` を自動実行
- `docs/test-design.md` を追加し、現時点のテスト方針を固定
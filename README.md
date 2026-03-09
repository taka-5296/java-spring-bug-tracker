# Bug Tracker（不具合管理アプリ）

## 概要

本システムは、Spring BootとPostgreSQLを用いたシンプルな不具合管理Webアプリです。

## まずやること（最短）

1) 起動：.\mvnw.cmd spring-boot:run
2) 疎通確認：GET http://localhost:8080/health → "OK"が出力

## 主要リンク

- [ローカル起動手順](#ローカル起動手順)
- [動作確認](#動作確認)
- [運用ルール（Git/GitHub）](#運用ルールgitgithub)
- [PRテンプレ](.github/PULL_REQUEST_TEMPLATE.md)
- [Issueテンプレ](.github/ISSUE_TEMPLATE.md)

## 主な機能

### 現時点


- ヘルスチェック：`GET /health`（OKを返す）
- Bug（チケット）のCRUD API（DB永続化）
  - 作成：`POST /api/bugs`
  - 一覧：`GET /api/bugs`
    - `status` クエリパラメータ指定でステータス絞り込み可能（例：`GET /api/bugs?status=OPEN`）
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
- DB
  - 永続化：PostgreSQL
  - 実装方式：JPA（ORM）
  - テーブル最小案：bugs（id, title, description, status, priority, createdAt, updatedAt）

### 予定

- Thymeleaf（画面表示）
- テスト：JUnit（Service単体テスト）
- CI：GitHub Actions
- Docker Compose

## ローカル起動手順

### 前提

- Java 17
- mvnw(Wrapper)

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

`\q`
※SELECTコマンドは`;`忘れに注意

### 4) アプリ起動（devプロファイル）

```PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

※ "-Dspring-boot.run.profiles=dev" は必ず "" で囲む（PowerShell対策）

### 5) 停止

- アプリ停止：Ctrl + C
- DB停止（任意）：`docker stop bug-tracker-postgres`

## API（暫定）

- POST /api/bugs
- GET /api/bugs
  - 任意クエリ：`status`（`OPEN` / `IN_PROGRESS` / `DONE`）
  - 任意クエリ：`page`（0始まりのページ番号）
  - 任意クエリ：`size`（1ページ件数）
  - 成功レスポンス：`items + meta`
- GET /api/bugs/{id}
- PUT /api/bugs/{id}
- DELETE /api/bugs/{id}

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

VALIDATION_ERROR : DTO Validation 失敗（400）
INVALID_JSON : JSON形式不正 / enum不正（400）
NOT_FOUND : 指定idのBugが存在しない（404）
INTERNAL_ERROR : 想定外エラー（500）

### ロギングポリシー

- Controller / Service の操作開始と主要結果を INFO で記録する
- 400 / 404 は想定内エラーとして INFO で記録する
- 500 は想定外エラーとして ERROR で記録し、スタックトレースを残す
- request body 全文、個人情報、巨大ペイロード、内部例外の生メッセージはログへ出しすぎない

## 動作確認

### ヘルスチェック

- ブラウザでアクセス：`http://localhost:8080/health`
- 期待結果：`OK` が表示される

### API (Bug作成・一覧・個別)

- 以下は PowerShellの例(Windows11想定 / curl使用)

### bugsテーブル作成と確認（psql）

#### テーブル作成

```PowerShell
Get-Content .\docs\db\bugs.sql | docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker`
```

- 期待結果
CREATE TABLE 等が表示され、エラーが出ない。
（※コマンド実行は/bug-tracker上で行う）

#### 確認

- psql内に入る
`docker exec -it bug-tracker-postgres psql -U bug_user -d bug_tracker`
- psql内で以下を実行
- `\dt`　Name = bugs というテーブルが表で確認できる
- `\q` psqlから抜ける

### Bug作成（POST）

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"test bug","description":"created by curl"}'
```

- 期待結果
`HTTP/1.1 201`と`Location`、および作成されたBugのJSONがコマンドラインに返る。

### Bug一覧取得（GET）

```PowerShell
curl.exe "http://localhost:8080/api/bugs"
```

- 期待結果
`"HTTP/1.1 200"` と、items にBug一覧、meta にページ情報を含むJSONが返る。

#### Bug一覧絞り込み取得（GET / status）

```PowerShell
curl.exe "http://localhost:8080/api/bugs?status=OPEN"
```

- 期待結果
"HTTP/1.1 200" と、status が OPEN のBugのみが items に入り、meta にページ情報が返る。

#### Bug一覧ページング取得（GET / page,size）

```PowerShell
curl.exe "http://localhost:8080/api/bugs?page=0&size=2"
```

- 期待結果
1ページあたり2件で items が返り、meta.page=0, meta.size=2 になる。

#### Bug一覧ページング取得（GET / page,size,status）

```PowerShell
curl.exe "http://localhost:8080/api/bugs?status=OPEN&page=0&size=2"
```

- 期待結果
status 絞り込みとページングを併用した結果が items + meta 形式で返る。

### Bug個別取得（GET）

```PowerShell
curl.exe -i "http://localhost:8080/api/bugs/{id}"
```

※`{id}`にはPOSTのレスポンスのidを使用する。

- 期待結果（GET）
`"HTTP/1.1 200"`と、{id}で指定したBugのJSONが返る。

### Bug更新（PUT）

```PowerShell
curl.exe -i -X PUT "http://localhost:8080/api/bugs/{id}" -H "Content-Type: application/json" --data-raw '{"title":"updated title","description":"updated by curl","status":"DONE","priority":"HIGH"}'
```

※`{id}`にはPOSTのレスポンスのidを使用する。

- 期待結果
`HTTP/1.1 200` と、更新後のBugのJSONが返る。

### Bug削除（DELETE）

```PowerShell
curl.exe -i -X DELETE "http://localhost:8080/api/bugs/{id}"
```

※`{id}`にはPOSTのレスポンスのidを使用する。

- 期待結果
`HTTP/1.1 204`（No Content）が返る。

#### 削除後の確認（GET → 404）

```PowerShell
curl.exe -i -X GET "http://localhost:8080/api/bugs/{id}"
```

- 期待結果
`HTTP/1.1 404`
`code=NOT_FOUND`のエラーJSONが返る

### Validation / エラー確認（400）

#### 作成時のValidationエラー（POST）

```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"","description":"created by curl"}'
```

- 期待結果
`HTTP/1.1 400`
`code=VALIDATION_ERROR` のエラーJSONが返る
details に `title must not be blank` が含まれる

#### 更新時のValidationエラー（PUT）

```PowerShell
curl.exe -i -X PUT "http://localhost:8080/api/bugs/{id}" -H "Content-Type: application/json" --data-raw '{"title":"","description":"updated by curl","status":"DONE","priority":"HIGH"}'
```

※`{id}`にはPOSTのレスポンスのidを使用する。

- 期待結果
`HTTP/1.1 400`
`code=VALIDATION_ERROR` のエラーJSONが返る

#### 不正JSON / enum不正（PUT）

```PowerShell
curl.exe -i -X PUT "http://localhost:8080/api/bugs/{id}" -H "Content-Type: application/json" --data-raw '{"title":"updated title","description":"updated by curl","status":"AAA","priority":"HIGH"}'
```

- 期待結果
HTTP/1.1 400
code=INVALID_JSON のエラーJSONが返る

## 運用ルール（Git/GitHub）

- ブランチ：main + feature/xxx
- 原則：mainは直接触らない。featureで作業 → PR → merge
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

## 今日の変更点（Daily Log）

- （ここに毎日追記する）

### Week1

- 2026-02-20: Git/GitHub初期化、.gitignore整備、PR→mergeを1回実施、README+Issues整備
- 2026-02-21: Spring Boot雛形作成、GET /health（OK）追加、mvnwで起動手順をREADMEに追記
- 2026-02-22: Bug作成・一覧の最小API（DBなしin-memory）を実装。POST/GET疎通を確認
- 2026-02-23: DTO+Validation導入（title必須）、例外ハンドリングで400のエラー形式統一（VALIDATION_ERROR / INVALID_JSON）、status/priority未指定はOPEN/LOWを自動補完
- 2026-02-24: GET /api/bugs/{id} 追加、存在しないIDは404を統一形式で返却（NOT_FOUND）
- 2026-02-25: /.github作成。PR&ISSUEテンプレ整理、READMEに[主要リンク]追加
- 2026-02-26: Week1成果総括（API最小セット/Validation/例外統一/テンプレ整備）。Week2のIssueを起票

### Week2

- 2026-02-27: DockerでPostgreSQL起動、psql疎通(SELECT 1/version)、pomにJPA+PostgreSQL追加、dev起動でHikari接続。READMEに手順追記。
- 2026-02-28: docs v0（要件定義/API/エラー）を追加。、API仕様を「現状/予定」に分離して契約を整理。
- 2026-03-01: docs v0（ER図/スキーマ/設計判断ログ）を追加。bugs テーブルをDDLで作成→psqlでINSERT/SELECT確認＋再現用SQLを保存。
- 2026-03-02: Bugの作成・一覧・詳細をDB永続化へ切替。Postman＋psqlで登録・取得を確認。
- 2026-03-03: Bugの詳細取得（GET /api/bugs/{id}）と更新（PUT /api/bugs/{id}）を追加。404/500を統一形式（details対応）で返却。BugResponseにdescription/updatedAtを追加し、API契約とREADMEを整合。
- 2026-03-04: Bug削除（DELETE /api/bugs/{id}）を追加してCRUD完成。READMEにCRUD通し確認手順（DELETE/404確認）を追記。
- 2026-03-05: READMEのDB初期化手順をPowerShell互換に固定（Get-Content|docker exec。400（VALIDATION_ERROR/INVALID_JSON）統一とPOST 201+Locationを実装。week2の実装内容docsに整合。

### Week3

- 2026-03-06: (GET /api/bugs)に statusクエリパラメータによる絞り込みを追加。（/api/bugs?status=OPEN|IN_PROGRESS|DONE）で取得可能。
- 2026-03-07: (GET /api/bugs) にページング（page/size）を追加。一覧レスポンスを `items + meta` 形式へ変更し、status絞り込みとの併用を curl で確認。
- 2026-03-08: 作成/更新DTOのValidationおよび、400エラーのdetails整形を確認。
- 2026-03-09: 例外ハンドリング整理（404/400/500）＋ログ粒度調整

## 週次まとめ（Weekly Log）

### Week1 (02-20 ~ 02-26)

- 到達点：/health、Bug最小API（作成/一覧/個別）、Validation（400統一）、不正JSON（400統一）、NotFound（404統一）、PR/issueテンプレ整備

### Week2 (02-27 ~ 03-05)

- 到達点：PostgreSQL（Docker）+ JPA永続化でBugのCRUD（作成/一覧/個別/更新/削除）を完成し、400/404/500のエラー形式を統一、POSTは201+Locationに固定。SSOT docsとREADMEの再現手順を整備。
# Bug Tracker（不具合管理アプリ）

## 概要

本システムは、Spring BootとPostgreSQLを用いたシンプルな不具合管理Webアプリです。

## まずやること（最短）

1. 起動

```PowerShell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

→ 起動状態になる

2.疎通確認

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
  - Service単体テスト（作成の正常系を追加）
- DB
  - 永続化：PostgreSQL
  - 実装方式：JPA（ORM）
  - テーブル最小案：bugs（id, title, description, status, priority, createdAt, updatedAt）

### 予定

- Thymeleaf（画面表示）
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
- DB　再起動（任意）：`docker start bug-tracker-postgres`

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

## テスト実行

### 単体テスト実行

```PowerShell
.\mvnw.cmd test
```

- 期待結果
BUILD SUCCESS が表示される。

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
curl.exe "http://localhost:8080/api/bugs?status=OPEN&priority=HIGH&keyword=api""
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
- 2026-03-10: (GET /api/bugs)にpriority絞り込みと、keyward検索および、それらの複合検索を追加。
- 2026-03-11: 絞り込み検索(status/priority/keyword/pageable)に動的クエリを採用し、custom repositoryを追加。README に検索例と既知制約を反映。
- 2026-03-12: 検索/ページング/Validation/例外/ログの通し確認を完了し、README の再現手順を整備して Week3 の品質を固定。

### Week4

- 2026-03-13: JUnit 5 / Mockito による BugService の単体テスト基盤を追加。create正常系で、status/priority 未指定時の OPEN/LOW 自動補完をテスト。

## 週次まとめ（Weekly Log）

### Week1 (02-20 ~ 02-26)

- 到達点：/health、Bug最小API（作成/一覧/個別）、Validation（400統一）、不正JSON（400統一）、NotFound（404統一）、PR/issueテンプレ整備

### Week2 (02-27 ~ 03-05)

- 到達点：PostgreSQL（Docker）+ JPA永続化でBugのCRUD（作成/一覧/個別/更新/削除）を完成し、400/404/500のエラー形式を統一、POSTは201+Locationに固定。SSOT docsとREADMEの再現手順を整備。

### Week3 (03-06 ~ 03-12)

- 到達点：Bug一覧に検索（status/priority/keyword）とページング（page/size）を追加し、一覧レスポンスを items + meta に固定。Validation・統一エラー形式（400/404/500）・ログ方針を整理し、README の再現手順まで整備。

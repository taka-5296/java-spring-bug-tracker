# Bug Tracker（不具合管理アプリ）

## 概要
本システムは、Spring Bootで作るシンプルな不具合管理Webアプリです。
※ 現時点はDB未接続（メモリ保存）。永続化はPostgreSQL導入後に対応します。

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
- Bug（チケット）の最小API（DBなし・メモリ保存）
  - 作成：`POST /api/bugs`
  - 一覧：`GET /api/bugs`
  - 個別：`GET /api/bugs/{id}`
  - 入力バリデーション（400）：title必須（VALIDATION_ERROR）
  - 不正JSON（400）：壊れたJSONまたは不正値（INVALID_JSON）
  - 存在しないID（404）：NOT_FOUND
  - status/priority未指定時は Service 側で OPEN/LOW をデフォルト補完

### 予定
- 不具合（Bug）のCRUD：作成 / 一覧 / 詳細 / 更新 / 削除
- ステータス管理：Open / In Progress / Done
- 認証・権限：USER / ADMIN
- 永続化：PostgreSQL

## 技術スタック
### 現時点
- Java 17
- Spring Boot（Web）
- Maven Wrapper（mvnw）
- DB（予定）
  - 永続化：PostgreSQL
  - 実装方式：JPA（ORM）で進める
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

### 起動方法（ローカル）
1. 起動
   - リポジトリ直下にて以下コマンドを実行
   - `.\mvnw.cmd spring-boot:run`
2. 停止
   - `Ctrl + C`

## API（暫定）
- POST /api/bugs
- GET /api/bugs
- GET /api/bugs/{id}

## エラーレスポンス形式
- 400: VALIDATION_ERROR / INVALID_JSON
- 404: NOT_FOUND
- 例のJSON（1つだけ）

### 動作確認

#### ヘルスチェック
- ブラウザでアクセス：`http://localhost:8080/health`
- 期待結果：`OK` が表示される

#### API (Bug作成・一覧・個別)
- 以下は PowerShellの例(Windows11想定 / curl使用)

##### Bug作成（POST）
```PowerShell
curl.exe -i -X POST "http://localhost:8080/api/bugs" -H "Content-Type: application/json" --data-raw '{"title":"test bug","description":"created by curl"}'
```
- 期待結果
`HTTP/1.1 200" //(または201)`と、作成されたBugのJSONがコマンドラインに返る。

##### Bug一覧（GET）
```PowerShell
curl.exe "http://localhost:8080/api/bugs"
```
- 期待結果
`"HTTP/1.1 200"` と、過去に作成済みのBugがJSONで返る。

##### Bug個別（GET：存在しないid）
```PowerShell
curl.exe -i "http://localhost:8080/api/bugs/{id}"
```
- 期待結果（GET）
`"HTTP/1.1 200"`と、{id}で指定したBugのJSONが返る。
※ Postmanでも同等の確認が可能（コレクションで実施）

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
- README：毎日「今日の変更点」に1〜3行追記

## 今日の変更点（Daily Log）
- （ここに毎日追記する）
- 2026-02-20: Git/GitHub初期化、.gitignore整備、PR→mergeを1回実施、README+Issues整備
- 2026-02-21: Spring Boot雛形作成、GET /health（OK）追加、mvnwで起動手順をREADMEに追記
- 2026-02-22: Bug作成・一覧の最小API（DBなしin-memory）を実装。POST/GET疎通を確認
- 2026-02-23: DTO+Validation導入（title必須）、例外ハンドリングで400のエラー形式統一（VALIDATION_ERROR / INVALID_JSON）、status/priority未指定はOPEN/LOWを自動補完
- 2026-02-24: GET /api/bugs/{id} 追加、存在しないIDは404を統一形式で返却（NOT_FOUND）
- 2026-02-25: /.github作成。PR&ISSUEテンプレ整理、READMEに[主要リンク]追加
- 2026-02-26: Week1成果総括（API最小セット/Validation/例外統一/テンプレ整備）。Week2のIssueを起票。

## 週次まとめ（Weekly Log）
#### Week1 (02-20 ~ 02-26: 
 - 到達点：/health、Bug最小API（作成/一覧/個別）、Validation（400統一）、不正JSON（400統一）、NotFound（404統一）、PR/issueテンプレ整備
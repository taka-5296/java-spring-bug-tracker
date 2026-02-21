# Bug Tracker（不具合管理アプリ）

## 概要
Spring Boot と PostgreSQL を用いた、シンプルな不具合管理Webアプリです。

## 主な機能
###　現時点

### 予定
- 不具合（Bug）のCRUD：作成 / 一覧 / 詳細 / 更新 / 削除
- ステータス管理：Open / In Progress / Done
- 認証・権限：USER / ADMIN

## 技術スタック
### 現時点
- Java 17
- Spring Boot（Web）
- Maven Wrapper（mvnw）

### 予定
- PostgreSQL（永続化）
- Thymeleaf（画面表示）
- テスト：JUnit（Service単体テスト）
- CI：GitHub Actions
- Docker Compose

## ローカル起動手順
### 前提
- Java 17
- mvnw(Wrapper)
- PostgreSQL（※DB導入後に追記）

### 起動方法（ローカル）
1. 起動
   - リポジトリ直下にて以下コマンドを実行
   - `.\mvnw.cmd spring-boot:run`
2. 停止
   - `Ctrl + C`

### 動作確認
- ブラウザでアクセス：`http://localhost:8080/health`
- 期待結果：`OK` が表示される

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

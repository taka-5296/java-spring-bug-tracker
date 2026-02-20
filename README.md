# Bug Tracker（不具合管理アプリ）

## 概要
Spring Boot と PostgreSQL を用いた、シンプルな不具合管理Webアプリです。

## 主な機能
// - 不具合（Bug）のCRUD：作成 / 一覧 / 詳細 / 更新 / 削除
// - ステータス管理（予定）：Open / In Progress / Done
// - 認証・権限（予定）：USER / ADMIN

## 技術スタック
- Java 17 
//- Maven
//- Spring Boot（MVC）
//- Thymeleaf（画面表示）
//- PostgreSQL（永続化）
//- テスト：JUnit（Service単体テスト）
//- CI：GitHub Actions（予定）
//- Docker Compose（予定）


## ローカル起動手順
### 前提
- Java 17
- Maven
- PostgreSQL（※DB導入後に追記）

### 起動

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
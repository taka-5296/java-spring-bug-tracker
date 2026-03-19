# 運用手順書（ローカル） - v0.2

## 目的

- ローカル環境でのテスト実行、CI失敗時の初動、ログ確認、DB確認、相談・障害報告の型をまとめる
- アプリ起動手順とAPIの詳細な動作確認は README を参照する

## 参照先

- [起動手順・疎通確認・CRUD確認](../README.md)
- [API契約](api-spec-v0.md)
- [エラー契約](error-spec-v0.md)
- [テスト設計](test-design-v0.md)

## テスト / CI

### ローカルでテストを実行する

- Windows PowerShell

```PowerShell
    .\mvnw.cmd test
```

- 期待結果
  - `BUILD SUCCESS` が表示される

### CIの実行概要

- `push` / `pull_request` を契機に GitHub Actions が自動実行される
- workflow 名: `bug-tracker-test`
- 実行内容
  - Java 17 をセットアップ
  - PostgreSQL service を起動
  - `SPRING_PROFILES_ACTIVE=test` で `./mvnw test` を実行

### CI失敗時の切り分け（5項目）

#### 1. Java

- Java 17 前提になっているか
- ローカルと CI の Java バージョン差がないか

#### 2. Maven

- `.\mvnw.cmd test` がローカルで再現するか
- 依存追加後に wrapper / pom.xml の不整合がないか

#### 3. DB

- PostgreSQL 接続設定が test プロファイル前提で揃っているか
- DB前提のテストがローカル起動中の dev DB に依存していないか

#### 4. 環境変数 / プロファイル

- `SPRING_PROFILES_ACTIVE=test` 前提の設定漏れがないか
- dev 用設定を test 側へ混ぜていないか

#### 5. テスト不安定

- テスト同士が順序依存になっていないか
- 固定 ID / 共有状態 / 実行順依存がないか

## ログ確認

### 最低限の見方

- INFO
  - 正常系の処理、起動完了、主要処理の通過点を確認する
- ERROR
  - 例外発生時の原因メッセージとスタックトレースを確認する

### 調査順

- まず `ERROR` ログを確認する
- 次に、直前の `INFO` ログを確認する
- 400 / 404 は想定内エラーとして INFO 側を見る
- 500 は想定外エラーとして ERROR 側を優先して見る

### ログ確認メモ

- 現時点では INFO に `called / succeeded` を残している
- 後続で必要なら `operation / targetId / result` などへ統一する

## DB確認

### PostgreSQL コンテナの起動・停止

- 初回起動

```PowerShell
    docker run --name bug-tracker-postgres `
      -e POSTGRES_DB=bug_tracker `
      -e POSTGRES_USER=bug_user `
      -e POSTGRES_PASSWORD=bug_pass `
      -p 5432:5432 `
      -d postgres:16
```

- 再起動

```PowerShell
    docker start bug-tracker-postgres
```

- 停止

```PowerShell
    docker stop bug-tracker-postgres
```

### コンテナ起動確認

- 確認コマンド

```PowerShell
    docker ps --filter "name=bug-tracker-postgres"
```

- 期待結果
  - `bug-tracker-postgres` が表示され、STATUS が `Up ...` になっている

### psql接続

- 接続コマンド

```PowerShell
    docker exec -it bug-tracker-postgres psql -U bug_user -d bug_tracker
```

### テーブル確認

- psql 内で実行
    `\dt`

- 期待結果
  - `bugs` テーブルが確認できる

### 終了

- psql 内で実行

    `\q`

### bugsテーブル作成

- SQL 流し込み

```PowerShell
    Get-Content .\docs\db\bugs.sql | docker exec -i bug-tracker-postgres psql -U bug_user -d bug_tracker
```

- 期待結果
  - `CREATE TABLE` 等が表示され、エラーが出ない

## トラブルシュート（最小）

### アプリが起動しない

- まず `.\mvnw.cmd test` が通るか確認する
- 直近の ERROR ログを確認する
- dev プロファイル指定と DB 起動状態を確認する
- 詳細な起動手順は `README.md` を参照する

### DBに接続できない

- `docker ps` で PostgreSQL コンテナが起動しているか確認する
- `docker logs bug-tracker-postgres` でエラー有無を確認する
- 接続先 DB 名、ユーザー名、パスワードの差異を確認する

### CIだけ落ちる

- ローカルで `.\mvnw.cmd test` を再実行する
- Java / Maven / DB / test profile の差を順に確認する
- `ci.yml` とローカル前提がずれていないか確認する

## 相談文テンプレ

- 目的:
- 発生している事実:
- 試したこと:
- 仮説:
- 困っている点:
- 次に見る点:

## 障害報告テンプレ

- 発生事象:
- 影響範囲:
- 発生時刻:
- 再現有無:
- 直近変更:
- 原因候補:
- 暫定対応:
- 次の確認:
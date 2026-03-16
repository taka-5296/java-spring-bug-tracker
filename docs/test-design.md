# テスト設計書(v0.1) - Bug Tracker

## 目的

- Bug Tracker の主要な業務判断を壊さない
- 変更時に、最低限守るべき振る舞いを継続的に確認できる状態を保つ
- PR 上で「何を、どこまで確認しているか」を第三者が理解できるようにする

## 対象レイヤ

### 現時点の主対象

- Service 層の単体テスト
  - 対象: `BugService`
  - 方針: Repository を Mockito のモックに置き換え、業務判断と委譲を確認する

### 今後の追加対象

- Repository を含む結合テスト（最小）
- HTTP レベルの確認（Controller / エラー形式）
- GlobalExceptionHandler の確認

## テストの種類

### 現時点

- Service 単体テスト

### 実装予定（未実装）

- DB を含む最小結合テスト（Repository層）
- HTTP レベルの確認テスト（Controller層）
- 例外ハンドリングの確認テスト

## 現時点で守る観点

### BugService

- 正常系
  - `create`: `status` / `priority` 未指定時に `OPEN` / `LOW` を自動補完する
  - `findAll`: keyword を正規化し、Repository の検索処理へ委譲する
  - `findById`: 既存 Bug を返す
  - `updateById`: 既存 Bug を更新して保存する
  - `deleteById`: 既存 Bug を削除する

- 異常系
  - `findById`: 未存在 id の場合に `BugNotFoundException` を送出する
  - `updateById`: 未存在 id の場合に `BugNotFoundException` を送出し、`save` を呼ばない
  - `deleteById`: 未存在 id の場合に `BugNotFoundException` を送出し、`delete` を呼ばない

## 今回やらないこと

- 広い Controller テストを大量に追加する
- 重い結合テストを一気に増やす
- 大量 Fixture や複雑な前提データに依存する

## テスト記述ルール

### 記述スタイル

- AAA（Arrange / Act / Assert）で記述する

### 命名方針

- テスト名は `(対象メソッド名)_should_...` を基本とする
- 1テスト1責務を意識し、保証内容が名前だけで分かるようにする

## 実行方法

### ローカル（Windows PowerShell）

```powershell
.\mvnw.cmd test
```

## CI（GitHub Actions）

- `push` および `pull_request` を契機に自動実行する
- GitHub Actions 上では Linux runner を用いるため、`./mvnw test` を実行する
- ローカルの Windows PowerShell では `.\mvnw.cmd test` を実行する

## CIとの関係

- ローカルで通るテストを、GitHub 上でも継続的に確認する
- PR で自動テスト結果を見える化し、変更の安全性を判断しやすくする

## CI アクション選定理由

本プロジェクトの CI では、現時点で `actions/checkout@v5` と `actions/setup-java@v4` を採用する。

### 選定理由

- `actions/checkout`
  - ワークフロー実行環境に対象リポジトリのソースコードを配置するために必要
  - GitHub Actions 上で `mvnw` や `pom.xml` を利用する前提となる

- `actions/setup-java`
  - Java 17 の実行環境を GitHub Actions runner 上に準備するために必要
  - Maven キャッシュを併用し、CI 実行時間を抑えやすい

- `distribution: temurin`
  - `setup-java` の一般的なJDK 配布であり、JDK 配布であり、Spring Boot / Maven 構成との相性がよい
  - 本プロジェクトでは特定ベンダ依存ない、標準寄りの選択として採用

### バージョン方針

- 現時点では、GitHub の Java + Maven 向け公式チュートリアルに近い `checkout@v5 + setup-java@v5` を採用
- `setup-java@v4`はNode.jaの延長サポートが2026年4月30日に終了するため、除外
- action リポジトリ単体ではより新しい major version も存在するが、「公式例との整合」「安定した最小構成」を優先

## 今後の追記予定

- DB を含む確認観点
- HTTP レベルの確認観点
- E2Eテストや本番相当の自動試験の設計

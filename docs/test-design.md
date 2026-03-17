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

- Service + Repository + DB の結合テスト（最小）
  - 対象: `BugServiceIntegrationTest`
  - 方針: 実際の Repository / DB を用い、接続の壊れを最小フローで検知する

- HTTP レベルの確認テスト
  - 対象: `BugControllerTest`
  - 方針: MockMvc を用い、Controller の HTTP 境界（ステータス / JSON / エラー形式）を確認する

### 今後の追加対象

- update / delete を含む追加の結合テスト
- GET / PUT / DELETE を含む HTTP レベルの確認テスト拡張
- GlobalExceptionHandler の確認強化
- E2E テスト

## テストの種類

### 現時点

- Service 単体テスト
- DB を含む最小結合テスト
- HTTP レベルの確認テスト（MockMvc）

### 実装予定（未実装）

- 例外ハンドリングの確認強化
- 追加の結合テスト（update / delete など）
- E2E テストや本番相当の自動試験

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

### DB を含む最小結合テスト

- 正常系
  - `create -> findById`: Service -> Repository -> DB の連携で保存と再取得が成立することを確認する
  - 保存後に id が採番されることを確認する
  - 再取得した Bug の title / description / status / priority が期待値と一致することを確認する

- 方針
  - 結合テストは「DB を含む接続の壊れ」を早期検知するために置く
  - 1本のテストで複数機能を詰め込みすぎず、最小フローで確認する
  - 重い結合テストは少数精鋭とし、一気に増やしすぎない

### HTTP レベルの確認テスト

- 成功系
  - `POST /api/bugs`: `201 Created` を返すこと
  - `Location` ヘッダに作成 Bug の URL が入ること
  - レスポンス JSON に `id / title / description / status / priority` が含まれること

- 異常系
  - title が空のとき `400 Bad Request` を返すこと
  - エラー JSON の `code` が `VALIDATION_ERROR` であること
  - エラー JSON に `message` と `details` が含まれること

- 方針
  - Controller テストは MockMvc を用い、HTTP 境界の契約を確認する
  - 広い Controller テストを大量に増やすのではなく、まずは代表的な成功系 / 異常系を最小で固定する

## 今回やらないこと

- 広い Controller テストを大量に追加する
- 重い結合テストを一気に増やす
- create / update / delete / 例外系を1本の結合テストへ詰め込む
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

- update / delete の結合テスト観点
- GET / PUT / DELETE を含む HTTP 確認観点
- Fixture / Helper の置き方
- Mockito を使う境界
- E2Eテストや本番相当の自動試験の設計

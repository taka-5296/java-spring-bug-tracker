# テスト設計書(v0.1) - Bug Tracker

## 目的

- Bug Tracker の主要な振る舞いを壊さない
- 変更時に、最低限守るべき確認観点を固定する
- PR 上で、何をどこまで確認しているかを第三者が把握できるようにする

## 対象レイヤ

### 現時点の主対象

- Service 単体テスト
  - 対象: `BugService`
  - 方針: Repository を Mockito のモックに置き換え、業務判断と委譲を確認する

- Service + Repository + DB の結合テスト（最小）
  - 対象: `BugServiceIntegrationTest`
  - 方針: Service -> Repository -> DB の接続を最小フローで確認する

- HTTP 確認テスト
  - 対象: `BugControllerTest`
  - 方針: MockMvc で HTTP 境界の契約を確認する

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
  - `create`: `status` / `priority` 未指定時に `OPEN` / `LOW` を補完する
  - `findAll`: keyword を正規化し、Repository の検索処理へ委譲する
  - `findById`: 既存 Bug を返す
  - `updateById`: 既存 Bug を更新して保存する
  - `deleteById`: 既存 Bug を削除する

- 異常系
  - `findById`: 未存在 id の場合に `BugNotFoundException` を送出する
  - `updateById`: 未存在 id の場合に `BugNotFoundException` を送出し、`save` を呼ばない
  - `deleteById`: 未存在 id の場合に `BugNotFoundException` を送出し、`delete` を呼ばない


### DB を含む最小結合テスト

- `create -> findById` の最小フローで、Service -> Repository -> DB の連携を確認する
- 保存後に id が採番されることを確認する
- 再取得した Bug の title / description / status / priority が期待値と一致すことを確認する
- 結合テストは「DB を含む接続の壊れ」を早期検知するために置く


### HTTP 確認テスト

- 成功系
  - `POST /api/bugs` が `201 Created` を返す
  - `Location` ヘッダに作成 Bug の URL が入る
  - レスポンス JSON に `id / title / description / status / priority` が含まれる

- 異常系
  - title が空のとき `400 Bad Request` を返す
  - エラー JSON の `code` が `VALIDATION_ERROR` である
  - エラー JSON に `message` と `details` が含まれる

## 今回やらないこと

- 広い Controller テストを大量に追加する
- 重い結合テストを一気に増やす
- create / update / delete / 例外系を1本の結合テストへ詰め込む
- 大量 Fixture や複雑な前提データに依存する

## 記述ルール

- AAA（Arrange / Act / Assert）で記述する
- テスト名は `(対象メソッド名)_should_...` を基本とする
- 1テスト1責務を意識し、保証内容が名前だけで分かるようにする

### Fixture / Helper の置き方

- テストデータ生成の重複を減らしたい場合は、`src/test/java` 配下のテスト専用クラスへ切り出す
- 複数レイヤのテストから共用する Fixture は、特定レイヤ配下ではなく `support` などの共通パッケージへ配置する
- 共通化するのは主にテストデータ生成までとし、`assertThat(...)` や `verify(...)` まで隠しすぎない
- テスト本文を読めば、何を保証しているか分かる状態を優先する

### Mockito を使う境界

- Service 単体テストでは Repository を Mockito のモックに置き換え、業務判断と委譲を確認する
- DB を含む結合テストでは Mockito を使わず、実際の Repository / DB を用いて接続を確認する
- Controller の HTTP 確認テストでは Service を Mockito のモックに置き換え、HTTP 境界の契約確認に集中する

## 実行方法

### ローカル（Windows PowerShell）

```powershell
.\mvnw.cmd test
```

## CI（GitHub Actions）

- `push` および `pull_request` を契機に自動実行する
- GitHub Actions 上では ./mvnw test を実行する

## CIとの関係

- ローカルで通るテストを、GitHub 上でも継続的に確認する
- PR で自動テスト結果を見える化し、変更の安全性を判断しやすくする

## CI アクションバージョン方針

- 現時点では、GitHub の Java + Maven 向け公式チュートリアルに近い `checkout@v5 + setup-java@v5` を採用
- `setup-java@v4`はNode.jaの延長サポートが2026年4月30日に終了するため、除外

## 今後の追記予定

- update / delete の結合テスト観点
- GET / PUT / DELETE を含む HTTP 確認観点
- Fixture / Helper の置き方
- Mockito を使う境界
- E2Eテストや本番相当の自動試験の設計

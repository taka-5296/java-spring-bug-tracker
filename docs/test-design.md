# Bug Tracker テスト設計

## 1. 目的

主要な振る舞いとレイヤ境界を、過剰なテスト数にせず継続的に確認する。

## 2. テスト構成

### Service単体テスト

対象: `BugServiceTest`

境界：

```text
BugService = real
BugRepository = Mockito mock
DB = 使用しない
```

主に確認するもの：

- status / priorityのデフォルト補完
- keyword正規化
- Repositoryへの委譲
- findById正常系
- update正常系
- delete正常系
- NotFound異常系

### DB結合テスト

対象: `BugServiceIntegrationTest`

境界：

```text
BugService = real
BugRepository = real
PostgreSQL = real
```

現在の代表テスト：

- `create_then_findById_should_reload_bug`

確認内容：

- Service経由で保存できる
- IDENTITYでIDが採番される
- `flush / clear` 後にDBから再取得できる
- 主要フィールドが一致する

設定：

- `@SpringBootTest`
- `@ActiveProfiles("test")`
- `@Transactional`
- DB: `bug_tracker_test`
- `ddl-auto=create-drop`

### Controller HTTP境界テスト   

対象: `BugControllerTest`

境界：

```text
Controller = real
BugService = Mockito mock
Repository / PostgreSQL = 使用しない
MockMvc = 使用
```

主に確認するもの：

- POST成功時の201
- Location header
- Response JSON
- Validation失敗時の400 / `VALIDATION_ERROR`

## 3. 現時点で未実装のテスト

- Controller → Service → Repository → PostgreSQLをHTTPから一貫して通すE2E相当テスト
- GET / PUT / DELETEのHTTP境界テスト拡張
- Securityの認証・認可テスト

これらは必要性に応じて追加し、テスト数を目的化しない。

## 4. Security実装時に追加する代表観点

G07でSecurity契約確定後、最低限次を追加する。

- `/health`: 未認証で成功
- `/api/bugs/**`: 未認証時の契約どおりの結果
- USER: 許可されたBug操作が成功
- USER: ADMIN限定操作が403
- ADMIN: ADMIN限定操作が成功

## 5. 記述ルール

- Arrange / Act / Assertを意識する
- テスト名は `method_should_behavior` を基本とする
- クラス名から明らかな情報をメソッド名へ重複して書きすぎない
- Fixtureは主にテストデータ生成へ限定する
- assert / verifyを共通Helperへ隠しすぎない

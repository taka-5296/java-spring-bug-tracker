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

対象:

- `BugServiceIntegrationTest`
- `UserRepositoryIntegrationTest`

#### BugServiceIntegrationTest

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

#### UserRepositoryIntegrationTest

境界：

```text
UserRepository = real
UserEntity = real
PostgreSQL = real
```

現在の代表テスト：

- `findByUsername_should_return_user`

確認内容：

- UserEntityをusersテーブルへ保存できる
- IDENTITYでIDが採番される
- usernameから既知ユーザーを取得できる
- role / enabledが保存値と一致する

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

### Security結合テスト

対象: `SecurityIntegrationTest`

境界：

```text
MockMvc = real
SecurityFilterChain = real
DBUserDetailsService = real
UserRepository = real
PostgreSQL = real
BugController = real
BugService = Mockito mock
```

主に確認するもの：

- /health の未認証アクセス
- 保護APIの未認証リダイレクト
- DBユーザーによるlogin成功
- password不一致時の認証失敗
- 未知usernameの認証失敗
- USERによる通常Bug API利用
- USERによるDELETE拒否
- ADMINによるDELETE許可
- CSRF token有無による更新系リクエストの差

## 3. 現時点で未実装のテスト

- Controller → Service → Repository → PostgreSQLをHTTPから一貫して通すE2E相当テスト
- GET / PUT / DELETEのHTTP境界テスト拡張

これらは必要性に応じて追加し、テスト数を目的化しない。

## 4. 記述ルール

- Arrange / Act / Assertを意識する
- テスト名は `method_should_behavior` を基本とする
- クラス名から明らかな情報をメソッド名へ重複して書きすぎない
- Fixtureは主にテストデータ生成へ限定する
- assert / verifyを共通Helperへ隠しすぎない

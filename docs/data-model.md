# Bug Tracker Data Model

## 1. 現行モデル

現在永続化するテーブルは `bugs` と `users` とする。

```text
bugs
----
id PK
title
description
status
priority
created_at
updated_at

users
-----
id PK
username UNIQUE
password_hash
role
enabled
```

現時点で他テーブルとの外部キー関連はない。

## 2. bugs

| column      | type              | null | default           | 備考          |
| ----------- | ----------------- | ---- | ----------------- | ------------- |
| id          | BIGINT / IDENTITY | no   | DB採番            | PK            |
| title       | VARCHAR(200)      | no   | -                 | Bugタイトル   |
| description | TEXT              | yes  | -                 | 詳細          |
| status      | VARCHAR(30)       | no   | `OPEN`            | `BugStatus`   |
| priority    | VARCHAR(30)       | no   | `LOW`             | `BugPriority` |
| created_at  | TIMESTAMPTZ       | no   | current timestamp | 作成日時      |
| updated_at  | TIMESTAMPTZ       | no   | current timestamp | 更新日時      |

### status

- `OPEN`
- `IN_PROGRESS`
- `DONE`

### priority

- `LOW`
- `MEDIUM`
- `HIGH`

## 3. JPAとの対応

- `BugEntity` ↔ `bugs`
- `id` は `GenerationType.IDENTITY`
- `status` / `priority` は `EnumType.STRING`
- `created_at` / `updated_at` は `OffsetDateTime`

アプリケーション側のenum値とDBへ格納される文字列を一致させる。

## 4. Schema管理方針

### dev

- DB: `bug_tracker`
- DDL: `docs/db/bugs.sql` / `docs/db/users.sql` を手動適用
- Hibernate: `ddl-auto=validate`
- データは保持する

### test

- DB: `bug_tracker_test`
- Hibernate: `ddl-auto=create-drop`
- テーブルはテスト起動時に作成し、終了時に破棄する
- `docs/db/bugs.sql` / `docs/db/users.sql` は自動実行しない
- 必要なテストデータはテストコード側で用意する

## 5. users

`users` はSpring SecurityのDB認証で使用する永続化モデルとする。

| column        | type              | null | default | 備考             |
| ------------- | ----------------- | ---- | ------- | ---------------- |
| id            | BIGINT / IDENTITY | no   | DB採番  | PK               |
| username      | VARCHAR(100)      | no   | -       | UNIQUE           |
| password_hash | VARCHAR(255)      | no   | -       | BCrypt hash      |
| role          | VARCHAR(30)       | no   | -       | `USER` / `ADMIN` |
| enabled       | BOOLEAN           | no   | `TRUE`  | ログイン利用可否 |

### DDL

実際のDDLとローカル開発用初期データは `docs/db/users.sql` で管理する。

### JPA対応方針

- `UserEntity` ↔ `users`
- id は `GenerationType.IDENTITY`
- role は `EnumType.STRING` で `USER` / `ADMIN` として保存する
- `UserRepository.findByUsername(String username)` でusername検索を行う
- `password_hash` はJava側では `passwordHash` として扱う
- 平文passwordはDBへ保存しない
- 現時点ではbugsとの外部キー関連は持たない

### 初期ユーザー

- devでは `docs/db/users.sql` からローカル開発・動作確認用のUSER / ADMINを投入する
- SQLへ保存するpasswordはBCrypt済みhashのみとし、平文passwordはDBおよびSQLへ保存しない
- testではdev用seedへ依存せず、必要なテストユーザーをテストコード側で用意する

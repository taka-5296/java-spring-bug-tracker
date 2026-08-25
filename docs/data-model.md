# Bug Tracker Data Model

## 1. 現行モデル

現在永続化するテーブルは `bugs` のみとする。

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
- DDL: `docs/db/bugs.sql` を手動適用
- Hibernate: `ddl-auto=validate`
- データは保持する

### test

- DB: `bug_tracker_test`
- Hibernate: `ddl-auto=create-drop`
- テーブルはテスト起動時に作成し、終了時に破棄する
- `docs/db/bugs.sql` は自動実行しない

## 5. 将来モデル

Security実装時に`users`永続化を検討する。bugsとの担当者・報告者関連は未確定。
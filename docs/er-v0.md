# ER図 v0.1（Bug Tracker）

## エンティティ

### bugs（本体）

- id (PK)
- title
- description
- status
- priority
- created_at
- updated_at

### users

- id (PK)
- username
- password_hash
- role
- created_at
- updated_at

## リレーション（v0）

- v0では認証未実装のため bugs は user に紐付けない
- 将来（v1+）：bugs.assignee_user_id -> users.id（担当者）
- 将来（v1+）：bugs.reporter_user_id -> users.id（報告者）

## メモ（説明用）

- users は拡張余地

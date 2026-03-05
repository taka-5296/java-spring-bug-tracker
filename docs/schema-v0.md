# スキーマ(v0.1) -Bug Tracker

## 方針（v0）
- 最小は bugs テーブルのみ作成
- users 今後作成予定
- 制約は PK + NOT NULL 中心（UNIQUE/FK/INDEXは後回し）

## bugs
| 列(カラム) | 型 | 制約 | デフォルト | 備考 |
|---|---|---|---|---|
| id | IDENTITY | PK | auto | DB採番 |
| title | VARCHAR(200) | NOT NULL |  | v0: title必須 |
| description | TEXT | NULL |  | 任意 |
| status | VARCHAR(30) | NOT NULL | 'OPEN' | enum相当（後でCHECK or enum型検討） |
| priority | VARCHAR(30) | NOT NULL | 'LOW' | enum相当 |
| created_at | TIMESTAMPTZ | NOT NULL | now() | 作成日時 |
| updated_at | TIMESTAMPTZ | NOT NULL | now() | 更新日時 |

## メモ
- status/priority の許容値はアプリ側 enum と合わせる（OPEN/IN_PROGRESS/DONE、LOW/MEDIUM/HIGH）
- updated_at の自動更新（トリガ）は v1+ で検討
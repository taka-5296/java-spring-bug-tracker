# API仕様書（v0.1）- Bug Tracker

Base URL: `http://localhost:8080`

## 0. 共通

- Content-Type: application/json
- 認証：現在未実装（追加予定）
- 形式：REST API（JSON）

---

## 1. 現状（2026-03-05時点実装済みAPI契約）

※この章は「今このリポジトリを起動して叩ける」ものだけを記載する。

### 1.1 エンドポイント一覧（現状）

| 操作 | メソッド | パス | リクエスト | 成功 | 失敗 |
| --- | --- | --- | --- | --- | --- |
| 作成 | POST | /api/bugs | CreateBugRequest | 200 + BugResponse | 400/500 |
| 一覧取得 | GET | /api/bugs | - | 200 + BugResponse[] | 500 |
| 個別取得 | GET | /api/bugs/{id} | - | 200 + BugResponse | 404/500 |
| 更新 | PUT | /api/bugs/{id} | UpdateBugRequest | 200 + BugResponse | 400/404 |
| 削除 | DELETE | /api/bugs/{id} | - | 204 (no body) | 404/500 |

> 注：現状は 201/500 を厳密に返す契約にはしていない（400の統一エラー（VALIDATION_ERROR/INVALID_JSON）は契約として定義済みだが、現時点の実装は404/500のみ。）。

### 1.2 データモデル（2026-03-05現在）

#### Bug（現状のレスポンス）

- id: number
- title: string
- description: string（null可）
- status: string（例：OPEN / IN_PROGRESS / DONE）
- priority: string（例：LOW / MEDIUM / HIGH）
- createdAt: string（ISO-8601）
- updatedAt: string (ISO-8601)

#### CreateBugRequest（現状のリクエスト）

- title: string（必須）
- description: string（任意）
- status: string（任意。未指定時はOPEN）
- priority: string（任意。未指定時はLOW）

#### UpdateBugRequest（現状のリクエスト）

- title: string（必須）
- description: string（任意）
- status: string（任意。未指定時はOPEN）
- priority: string（任意。未指定時はLOW）

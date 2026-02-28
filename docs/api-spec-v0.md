# API仕様書（v0）- Bug Tracker

Base URL: http://localhost:8080

## 0. 共通
- Content-Type: application/json
- 認証：v0ではなし（未実装）
- 形式：REST API（JSON）

---

## 1. 現状（Week1まで実装済みのAPI契約）
※この章は「今このリポジトリを起動して叩ける」ものだけを記載する。

### 1.1 エンドポイント一覧（現状）
| 操作 | メソッド | パス | リクエスト | 成功 | 失敗 |
|---|---|---|---|---|---|
| 作成 | POST | /api/bugs | CreateBugRequest（現状） | 200 + Bug（現状） | 400/404 |
| 一覧取得 | GET | /api/bugs | - | 200 + Bug[]（現状） | - |
| 個別取得 | GET | /api/bugs/{id} | - | 200 + Bug（現状） | 404 |

> 注：現状は 201/500 を厳密に返す契約にはしていない（GlobalExceptionHandlerは404/400を整備済み）。

### 1.2 データモデル（現状）
#### Bug（現状のレスポンス）
- id: number
- title: string
- description: string（null可）
- status: string（例：OPEN / IN_PROGRESS / DONE）
- priority: string（例：LOW / MEDIUM / HIGH）
- createdAt: string（ISO-8601）

#### CreateBugRequest（現状のリクエスト）
- title: string（必須）
- description: string（任意）
- status: string（任意。未指定時はOPEN）
- priority: string（任意。未指定時はLOW）

---

## 2. 予定（Week2で追加する“最終形の枠”）
※この章は「CRUD完成（DB永続化）に合わせて実装する」枠。実装したら「現状」へ移す。

### 2.1 エンドポイント一覧（予定）
| 操作 | メソッド | パス | リクエスト | 成功 | 失敗 |
|---|---|---|---|---|---|
| 作成 | POST | /api/bugs | CreateBugRequest | 201 + BugResponse | 400/500 |
| 一覧取得 | GET | /api/bugs | - | 200 + BugListResponse | 500 |
| 個別取得 | GET | /api/bugs/{id} | - | 200 + BugResponse | 404/500 |
| 更新 | PUT | /api/bugs/{id} | UpdateBugRequest | 200 + BugResponse | 400/404/500 |
| 削除 | DELETE | /api/bugs/{id} | - | 204 (no body) | 404/500 |

### 2.2 データモデル（予定）
#### BugResponse（予定：DTO）
- id: number
- title: string
- description: string（null可）
- status: string（許容値：OPEN / IN_PROGRESS / DONE）
- priority: string（許容値：LOW / MEDIUM / HIGH）
- createdAt: string（ISO-8601）

#### CreateBugRequest（予定）
- title: string（必須）
- description: string（任意）
- status: string（任意。未指定時はOPEN）
- priority: string（任意。未指定時はLOW）

#### UpdateBugRequest（予定）
- title: string（必須）
- description: string（任意）
- status: string（任意）
- priority: string（任意）
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
| 作成 | POST | /api/bugs | CreateBugRequest | 201 + BugResponse | 400/500 |
| 一覧取得 | GET | /api/bugs | status/priority/keyword/page/size (optional) | 200 + BugResponse[] | 500 |
| 個別取得 | GET | /api/bugs/{id} | - | 200 + BugResponse | 404/500 |
| 更新 | PUT | /api/bugs/{id} | UpdateBugRequest | 200 + BugResponse | 400/404 |
| 削除 | DELETE | /api/bugs/{id} | - | 204 (no body) | 404/500 |

- `POST /api/bugs` 失敗例：title 空 → 400 / VALIDATION_ERROR
- `PUT /api/bugs/{id}` 失敗例：status に不正enum → 400 / INVALID_JSON

> 注：現状は 500 を厳密に返す契約にはしていない

### 1.2 データモデル（2026-03-06現在）

#### Bug（現状のレスポンス）

- id: number
- title: string
- description: string（null可）
- status: string（例：OPEN / IN_PROGRESS / DONE）
- priority: string（例：LOW / MEDIUM / HIGH）
- createdAt: string（ISO-8601）
- updatedAt: string (ISO-8601)

##### BugPageResponse（現状の一覧レスポンス）

- items: Bug[]
- meta: PageMetaResponse

##### PageMetaResponse

- page: number（現在ページ。0始まり）
- size: number（1ページ件数）
- totalElements: number（総件数）
- totalPages: number（総ページ数）
- hasNext: boolean
- hasPrevious: boolean

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

#### 一覧取得のクエリパラメータ

- status: string（任意。`OPEN` / `IN_PROGRESS` / `DONE`）
- priority: string（任意。`LOW` / `MEDIUM` / `HIGH`）
- keyword: string（任意。未指定時は`null`）
- page: number（任意。未指定時は `0`）
- size: number（任意。未指定時は `10`）

- 例1: `GET /api/bugs`
- 例2: `GET /api/bugs?status=OPEN`
- 例3: `GET /api/bugs?page=0&size=2`
- 例4: `GET /api/bugs?status=OPEN&page=0&size=2`

#### 一覧取得レスポンス例

```json
{
  "items": [
    {
      "id": 1,
      "title": "test bug",
      "description": "created by curl",
      "status": "OPEN",
      "priority": "LOW",
      "createdAt": "2026-03-07T12:00:00Z",
      "updatedAt": "2026-03-07T12:00:00Z"
    }
  ],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 4,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
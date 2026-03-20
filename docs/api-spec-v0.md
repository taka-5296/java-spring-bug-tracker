# API仕様書（v0.1）- Bug Tracker

Base URL: `http://localhost:8080`

## 0. 共通

- Content-Type: application/json
- 認証/認可（導入予定の固定方針）
  - 未ログインで保護対象へアクセスした場合は 401
  - 認証済みだが権限不足の場合は 403
  - 削除操作は ADMIN ロールのみ許可予定
- 形式：REST API（JSON）

## 1. 現行API契約

※この章は、現時点でこのリポジトリを起動して利用できる API 契約を記載する。

### 1.1 エンドポイント一覧

| 操作     | メソッド | パス           | リクエスト                                   | 成功                  | 失敗        |
| -------- | -------- | -------------- | -------------------------------------------- | --------------------- | ----------- |
| 作成     | POST     | /api/bugs      | CreateBugRequest                             | 201 + BugResponse     | 400/500     |
| 一覧取得 | GET      | /api/bugs      | status/priority/keyword/page/size (optional) | 200 + BugPageResponse | 500         |
| 個別取得 | GET      | /api/bugs/{id} | -                                            | 200 + BugResponse     | 404/500     |
| 更新     | PUT      | /api/bugs/{id} | UpdateBugRequest                             | 200 + BugResponse     | 400/404/500 |
| 削除     | DELETE   | /api/bugs/{id} | -                                            | 204 (no body)         | 404/500     |

- `POST /api/bugs` の失敗例：title 空 → 400 / VALIDATION_ERROR
- `PUT /api/bugs/{id}` の失敗例：status に不正 enum → 400 / INVALID_JSON
- `GET / PUT / DELETE /api/bugs/{id}` で対象が存在しない場合：404 / NOT_FOUND

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
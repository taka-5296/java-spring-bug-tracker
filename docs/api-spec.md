# Bug Tracker API仕様書

Base URL: `http://localhost:8080`

## 1. 共通

- API形式: REST / JSON
- Request / Response: `application/json`
- Securityの最終契約は後日、追加予定。未認証時の401またはlogin redirectについては未決定。
- アプリケーションレベルのエラーは本書の「エラーレスポンス」に従う

## 2. エンドポイント

| 操作 | Method | Path             | 成功                    |
| ---- | ------ | ---------------- | ----------------------- |
| 作成 | POST   | `/api/bugs`      | 201 + `BugResponse`     |
| 一覧 | GET    | `/api/bugs`      | 200 + `BugPageResponse` |
| 個別 | GET    | `/api/bugs/{id}` | 200 + `BugResponse`     |
| 更新 | PUT    | `/api/bugs/{id}` | 200 + `BugResponse`     |
| 削除 | DELETE | `/api/bugs/{id}` | 204                     |

## 3. Request

### CreateBugRequest

| 項目        | 型     | 必須 | 備考                                             |
| ----------- | ------ | ---- | ------------------------------------------------ |
| title       | string | yes  | 空不可、最大200文字                              |
| description | string | no   | null可                                           |
| status      | string | no   | `OPEN` / `IN_PROGRESS` / `DONE`。未指定時 `OPEN` |
| priority    | string | no   | `LOW` / `MEDIUM` / `HIGH`。未指定時 `LOW`        |

### UpdateBugRequest

現時点では作成時と同じ入力方針とする。

| 項目        | 型     | 必須 | 備考                |
| ----------- | ------ | ---- | ------------------- |
| title       | string | yes  | 空不可、最大200文字 |
| description | string | no   | null可              |
| status      | string | no   | 未指定時 `OPEN`     |
| priority    | string | no   | 未指定時 `LOW`      |

## 4. Response

### BugResponse

- `id`: number
- `title`: string
- `description`: string / null
- `status`: `OPEN` / `IN_PROGRESS` / `DONE`
- `priority`: `LOW` / `MEDIUM` / `HIGH`
- `createdAt`: ISO-8601 datetime
- `updatedAt`: ISO-8601 datetime

### BugPageResponse

```json
{
  "items": [],
  "meta": {
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

## 5. 一覧検索

`GET /api/bugs`

| Query    | 型     | 既定値 | 内容                        |
| -------- | ------ | ------ | --------------------------- |
| status   | enum   | none   | status完全一致              |
| priority | enum   | none   | priority完全一致            |
| keyword  | string | none   | title / description部分一致 |
| page     | number | 0      | 0始まり                     |
| size     | number | 10     | 1ページ件数                 |

複数条件はANDで評価する。

keyword条件内では、titleまたはdescriptionのどちらかに一致すればよい。

例：

```text
GET /api/bugs?status=OPEN&priority=HIGH&keyword=login&page=0&size=10
```

## 6. 成功時のHTTPステータス

- POST: `201 Created`
  - `Location: /api/bugs/{id}` を返す
- GET: `200 OK`
- PUT: `200 OK`
- DELETE: `204 No Content`

## 7. エラーレスポンス

### ErrorResponse

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力値が不正です",
  "details": [
    "title must not be blank"
  ]
}
```

| HTTP | code               | 主な原因                   |
| ---- | ------------------ | -------------------------- |
| 400  | `VALIDATION_ERROR` | Bean Validation失敗        |
| 400  | `INVALID_JSON`     | JSON形式不正、enum変換失敗 |
| 404  | `NOT_FOUND`        | 指定Bugが存在しない        |
| 500  | `INTERNAL_ERROR`   | 想定外例外                 |

内部例外の詳細はレスポンスへ直接公開しない。

### Security由来の401 / 403

Security Filterで発生する401 / 403のレスポンス形式はG07で確定する。
現時点ではアプリケーション例外の `ErrorResponse` と同一形式であることを契約しない。

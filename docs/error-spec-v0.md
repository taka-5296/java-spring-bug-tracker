# Error Spec (v0.1) - Bug Tracker

## 統一エラーフォーマット（契約）

- すべてのエラーは以下のJSON形式で返す（成功レスポンスとは別枠）
- details は「フィールドエラー」または「補足情報」を入れる。不要なら空配列でもよい。

### ErrorResponse

- code: string（例：VALIDATION_ERROR / INVALID_JSON / NOT_FOUND / INTERNAL_ERROR）
- message: string（人間向けの短文）
- details: array（0..n）

## ステータスコード別の方針

### 400 Bad Request

- 入力不備（Validation）: code=VALIDATION_ERROR
- JSON不正/enum不正など: code=INVALID_JSON

### 404 Not Found

- 対象リソースが存在しない: code=NOT_FOUND

### 500 Internal Server Error

- 想定外例外: code=INTERNAL_ERROR
- message は詳細を出しすぎない（内部情報漏洩防止）

## JSON例（Validation）

```json
{
  "code": "VALIDATION_ERROR",
  "message": "入力値が不正です",
  "details": ["titleは必須です"]
}
```

```json
{
  "code": "INVALID_JSON",
  "message": "リクエストJSONの形式が不正です",
  "details": ["JSONの形式、またはenumの値を確認してください"]
}
```

```json
{
  "code": "NOT_FOUND",
  "message": "指定したBugが見つかりません",
  "details": []
}
```

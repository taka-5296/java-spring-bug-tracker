# 設計判断ログ(v0) -Bug Tracker

## D-001: status/priority のデフォルトをサーバ側で補完する
- 結論：未指定なら status=OPEN, priority=LOW で補完する
- 理由：クライアントの実装差で欠けても、API契約を壊さないため
- 代替案：クライアント必須にする（400にする）→ v0は採用しない
- 影響：Service層で補完。DBもNOT NULL + DEFAULTで整合させる
- 証拠：requirements と現状実装でこの方針を採用中

## D-002: 永続化は bugs テーブル単体から開始する（usersは現状箱だけ）
- 結論：v0では bugs のCRUDに集中し、usersテーブルは作らない
- 理由：認証/権限の実装後にusersに取り掛かる。今作ると手戻りの可能性大
- 代替案：最小usersも作る → 設計範囲が増え、CRUD実装が遅れる可能性
- 影響：bugsは user_id を持たない。将来カラム追加で拡張する
- リスク：後で移行が必要 → docs とDDLを残して対応を明確にする

## D-003: v0のid採番はDBの連番（BIGSERIAL）を採用する
- 結論：idはDB採番（連番）とする
- 理由：最小コストで一意性を担保でき、JPA移行も容易
- 代替案：UUID→学習コスト増。アプリ採番→競合や設計負債になりやすい
- 影響：insert時にidは指定しない運用になる
- リスク：公開APIで推測可能→ v0は内部用途想定で許容（将来検討）

## D-004: エラー形式は統一JSON（code/message/details）を維持する
- 結論：400/404/500は統一フォーマットで返す
- 理由：クライアントが分岐しやすく、運用時の解析も簡単
- 代替案：例外ごとに形を変える→クライアントが壊れやすい
- 影響：GlobalExceptionHandler中心に契約維持
- 証拠：error-spec に明記済み

## D-005: Controllerは入力DTO、Serviceはドメイン判断（補完/NotFound）を持つ
- 結論：ControllerはHTTP境界、Serviceは業務判断を担当
- 理由：責務分離でテストしやすく、DB導入後も変更点を局所化できる
- 代替案：Controllerに判断を寄せる→肥大化しやすい
- 影響：status/priority補完やNotFound例外はService側に置く
- 証拠：現状のBugController/BugServiceがこの層分離になっている
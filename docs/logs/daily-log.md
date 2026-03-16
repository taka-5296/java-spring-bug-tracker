## 今日の変更点（Daily Log）

- （ここに毎日追記する）

### Week1

- 2026-02-20: Git/GitHub初期化、.gitignore整備、PR→mergeを1回実施、README+Issues整備
- 2026-02-21: Spring Boot雛形作成、GET /health（OK）追加、mvnwで起動手順をREADMEに追記
- 2026-02-22: Bug作成・一覧の最小API（DBなしin-memory）を実装。POST/GET疎通を確認
- 2026-02-23: DTO+Validation導入（title必須）、例外ハンドリングで400のエラー形式統一（VALIDATION_ERROR / INVALID_JSON）、status/priority未指定はOPEN/LOWを自動補完
- 2026-02-24: GET /api/bugs/{id} 追加、存在しないIDは404を統一形式で返却（NOT_FOUND）
- 2026-02-25: /.github作成。PR&ISSUEテンプレ整理、READMEに[主要リンク]追加
- 2026-02-26: Week1成果総括（API最小セット/Validation/例外統一/テンプレ整備）。Week2のIssueを起票

### Week2

- 2026-02-27: DockerでPostgreSQL起動、psql疎通(SELECT 1/version)、pomにJPA+PostgreSQL追加、dev起動でHikari接続。READMEに手順追記。
- 2026-02-28: docs v0（要件定義/API/エラー）を追加。、API仕様を「現状/予定」に分離して契約を整理。
- 2026-03-01: docs v0（ER図/スキーマ/設計判断ログ）を追加。bugs テーブルをDDLで作成→psqlでINSERT/SELECT確認＋再現用SQLを保存。
- 2026-03-02: Bugの作成・一覧・詳細をDB永続化へ切替。Postman＋psqlで登録・取得を確認。
- 2026-03-03: Bugの詳細取得（GET /api/bugs/{id}）と更新（PUT /api/bugs/{id}）を追加。404/500を統一形式（details対応）で返却。BugResponseにdescription/updatedAtを追加し、API契約とREADMEを整合。
- 2026-03-04: Bug削除（DELETE /api/bugs/{id}）を追加してCRUD完成。READMEにCRUD通し確認手順（DELETE/404確認）を追記。
- 2026-03-05: READMEのDB初期化手順をPowerShell互換に固定（Get-Content|docker exec。400（VALIDATION_ERROR/INVALID_JSON）統一とPOST 201+Locationを実装。week2の実装内容docsに整合。

### Week3

- 2026-03-06: (GET /api/bugs)に statusクエリパラメータによる絞り込みを追加。（/api/bugs?status=OPEN|IN_PROGRESS|DONE）で取得可能。
- 2026-03-07: (GET /api/bugs) にページング（page/size）を追加。一覧レスポンスを `items + meta` 形式へ変更し、status絞り込みとの併用を curl で確認。
- 2026-03-08: 作成/更新DTOのValidationおよび、400エラーのdetails整形を確認。
- 2026-03-09: 例外ハンドリング整理（404/400/500）＋ログ粒度調整
- 2026-03-10: (GET /api/bugs)にpriority絞り込みと、keyward検索および、それらの複合検索を追加。
- 2026-03-11: 絞り込み検索(status/priority/keyword/pageable)に動的クエリを採用し、custom repositoryを追加。README に検索例と既知制約を反映。
- 2026-03-12: 検索/ページング/Validation/例外/ログの通し確認を完了し、README の再現手順を整備して Week3 の品質を固定。

### Week4

- 2026-03-13: JUnit 5 / Mockito による BugService の単体テスト基盤を追加。create正常系で、status/priority 未指定時の OPEN/LOW 自動補完をテスト。
- 2026-03-14: BugService の NotFound 異常系テストを追加。findById / updateById / deleteById で BugNotFoundException と後続未実行（save/delete未呼び出し）を確認。
- 2026-03-15: BugService の findAll/findById/updateById/deleteById の正常系testを追加。
- 2026-03-16: push / PR 時に行うCIの追加。docs/test-design.mdの作成。

## 週次まとめ（Weekly Log）

### Week1 (02-20 ~ 02-26)

- 到達点：/health、Bug最小API（作成/一覧/個別）、Validation（400統一）、不正JSON（400統一）、NotFound（404統一）、PR/issueテンプレ整備

### Week2 (02-27 ~ 03-05)

- 到達点：PostgreSQL（Docker）+ JPA永続化でBugのCRUD（作成/一覧/個別/更新/削除）を完成し、400/404/500のエラー形式を統一、POSTは201+Locationに固定。SSOT docsとREADMEの再現手順を整備。

### Week3 (03-06 ~ 03-12)

- 到達点：Bug一覧に検索（status/priority/keyword）とページング（page/size）を追加し、一覧レスポンスを items + meta に固定。Validation・統一エラー形式（400/404/500）・ログ方針を整理し、README の再現手順まで整備。

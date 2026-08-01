新しいAPIエンドポイント（CRUD一式）を作成する。

## 引数

$ARGUMENTS にリソース名が渡される（例: `User`, `Order`）

## 手順

1. 以下のファイルを CLAUDE.md の命名規則に従って作成:
   - `controller/${ARGUMENTS}Controller.java` — CRUD エンドポイント
   - `service/${ARGUMENTS}Service.java` — Service インターフェース
   - `service/${ARGUMENTS}ServiceImpl.java` — Service 実装
   - `repository/${ARGUMENTS}Repository.java` — Spring Data JPA Repository
   - `model/entity/${ARGUMENTS}.java` — JPA Entity
   - `model/dto/Create${ARGUMENTS}Request.java` — 作成リクエスト DTO
   - `model/dto/Update${ARGUMENTS}Request.java` — 更新リクエスト DTO
   - `model/dto/${ARGUMENTS}Response.java` — レスポンス DTO
2. CLAUDE.md のコード例（DTO の from/toEntity パターン、Service のインターフェース分離）に従う
3. 対応するテストクラスを作成:
   - `controller/${ARGUMENTS}ControllerTest.java`
   - `service/${ARGUMENTS}ServiceTest.java`
4. 作成した構成をユーザーに報告する

## ルール

- CLAUDE.md のレイヤー責務と依存方向を厳守する
- Entity を Controller で直接返さない
- エンドポイントは `/api/v1/` 配下にする

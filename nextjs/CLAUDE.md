# Next.js Project

## 技術スタック

- Next.js (App Router)
- TypeScript (strict mode)
- React
- ESLint (Flat Config) + Prettier

## ディレクトリ構成

```
src/
  app/                          # App Router ルーティング
    (auth)/                     # 認証系ルートグループ
      login/page.tsx
      register/page.tsx
    (main)/                     # メイン画面ルートグループ
      dashboard/page.tsx
      settings/page.tsx
    api/                        # Route Handlers
    layout.tsx
    page.tsx
  features/                     # 機能モジュール（★核心）
    auth/
      components/               # この機能専用のコンポーネント
      hooks/                    # この機能専用のhooks
      actions/                  # Server Actions
      lib/                      # この機能専用のユーティリティ
      types.ts                  # この機能の型定義
      index.ts                  # Public API（外部に公開するもの）
    dashboard/
      components/
      hooks/
      actions/
      types.ts
      index.ts
  components/                   # 共有コンポーネント
    ui/                         # プリミティブUI (Button, Input, Dialog)
    layout/                     # レイアウト (Header, Sidebar, Footer)
  hooks/                        # 共有hooks
  lib/                          # ユーティリティ、設定
  types/                        # グローバル型定義
```

## 設計ルール

### 機能モジュール (features/)

- **1機能 = 1ディレクトリ**: 関連するコンポーネント・hooks・actions・型をまとめる
- **外部公開は `index.ts` 経由**: 他の機能や `app/` から参照するものだけを `index.ts` で re-export する
- **機能間の直接参照は禁止**: `features/auth/` から `features/dashboard/` の内部を直接 import しない。共有が必要なら `components/` や `hooks/` に昇格させる

### SOLID原則の適用

- **SRP**: コンポーネント = 表示、hooks = 状態・ロジック、Server Actions = データ操作。1コンポーネントが表示とデータ取得の両方を担わない
- **OCP**: `children` や composition パターンで拡張する。条件分岐で機能を切り替えるより、コンポーネントを組み合わせる
- **ISP**: Props は必要最小限にする。巨大な props を渡すより、必要なフィールドだけを受け取る
- **DIP**: データ取得ロジックはhooksやServer Actionsに分離し、コンポーネントは「何を表示するか」だけに集中する

### コンポーネント設計

- Server Components をデフォルトとする。`"use client"` はインタラクションが必要な場合のみ
- Client Components は末端（リーフ）に寄せる。できるだけ小さく保つ
- 表示とロジックを分離する:

```tsx
// ❌ Bad: コンポーネント内にロジックが混在
export function UserList() {
  const [users, setUsers] = useState([]);
  useEffect(() => { fetch('/api/users').then(...) }, []);
  const filtered = users.filter(u => u.active);
  return <ul>{filtered.map(u => <li key={u.id}>{u.name}</li>)}</ul>;
}

// ✅ Good: hooks にロジックを分離
function useActiveUsers() {
  // fetch and filter logic
}

export function UserList() {
  const users = useActiveUsers();
  return <ul>{users.map(u => <li key={u.id}>{u.name}</li>)}</ul>;
}
```

### 命名規則

| 対象 | 規則 | 例 |
|---|---|---|
| コンポーネントファイル | PascalCase | `UserProfile.tsx` |
| hooks | camelCase, `use` prefix | `useAuth.ts` |
| Server Actions | camelCase, 動詞始まり | `createUser.ts` |
| ユーティリティ | camelCase | `formatDate.ts` |
| 型定義ファイル | camelCase | `types.ts` |
| 定数 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| コンポーネント名 | PascalCase | `UserProfile` |
| 関数名 | camelCase | `formatDate` |
| 型/interface名 | PascalCase | `UserProfile`, `CreateUserRequest` |

### Linter / Formatter

- **ESLint**: `eslint.config.mjs` で Flat Config 形式。`next/core-web-vitals` + `typescript-eslint/strict` をベースに適用
- **Prettier**: `.prettierrc.json` でフォーマットルールを定義。ESLint とは役割を分離（ESLint = コード品質、Prettier = フォーマット）
- **EditorConfig**: `.editorconfig` でエディタ間の基本設定を統一
- ESLint で `features/` の内部モジュールへの直接 import を `no-restricted-imports` で禁止している
- `import/order` で import 文の並び順を自動整理する
- コードフォーマットは Prettier に任せ、ESLint のフォーマット系ルールは使わない

### Do / Don't

- **Do**: `feature/` 内で完結する設計を目指す
- **Do**: 共有コンポーネントは汎用的に、機能固有のスタイルやロジックを持たせない
- **Do**: Server Actions にバリデーション（zod等）を入れる
- **Don't**: `app/` 配下にビジネスロジックを書かない。`app/` はルーティングとレイアウトに徹する
- **Don't**: `features/` を跨ぐ直接 import をしない
- **Don't**: 1ファイルに複数の公開コンポーネントを定義しない

## CI/CD

### CI パイプライン (.github/workflows/ci.yml)

PR・push 時に以下が並列実行され、すべて通過後に Build が走る:

1. **Lint & Format** — `pnpm run lint` + `pnpm run format:check`
2. **Type Check** — `pnpm run typecheck`
3. **Test** — `pnpm run test -- --coverage`
4. **Build** — `pnpm run build`（上記3ジョブ通過後）

### CD パイプライン (.github/workflows/cd.yml)

main ブランチで CI 成功後、Docker イメージをビルドし GHCR にプッシュする。

### 必須 npm scripts

CI/CD が期待する `package.json` の scripts:

```json
{
  "scripts": {
    "lint": "next lint",
    "format:check": "prettier --check .",
    "typecheck": "tsc --noEmit",
    "test": "vitest run",
    "build": "next build"
  }
}
```

### Docker

- `Dockerfile`: multi-stage ビルド（deps → build → runtime）
- Next.js の `output: "standalone"` を `next.config.ts` で有効にすること
- `.dockerignore` で不要ファイルを除外済み

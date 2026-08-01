# ai-dev-templates

AI駆動開発（Claude Code）のためのテンプレートリポジトリ。

## テンプレート

| テンプレート | 説明 |
|---|---|
| `nextjs/` | Next.js フロントエンド開発 |
| `springboot/` | Spring Boot バックエンド開発 |

## 使い方

1. このリポジトリをクローン
2. 使いたいテンプレートのディレクトリをプロジェクトにコピー
3. `CLAUDE.md` をプロジェクトに合わせてカスタマイズ

## CI/CD 構成

各テンプレートに GitHub Actions ベースの CI/CD パイプラインが含まれている。

### パイプライン全体像

```
PR / push → CI (並列チェック) → main マージ → CD (Docker イメージ → GHCR)
```

```
┌─────────────────── CI (.github/workflows/ci.yml) ───────────────────┐
│                                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │ Lint & Format │  │  Type Check  │  │     Test     │  ← 並列実行   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘               │
│         └─────────────────┼─────────────────┘                        │
│                           ▼                                          │
│                    ┌──────────────┐                                   │
│                    │    Build     │  ← 全ジョブ通過後                │
│                    └──────────────┘                                   │
└──────────────────────────────────────────────────────────────────────┘
                            │ CI 成功 (main のみ)
                            ▼
┌─────────────────── CD (.github/workflows/cd.yml) ───────────────────┐
│                                                                      │
│  ┌──────────────────────────────────────────────────────────┐        │
│  │  Docker Build (multi-stage) → Push to GHCR               │        │
│  │  Tags: git SHA + latest                                   │        │
│  └──────────────────────────────────────────────────────────┘        │
└──────────────────────────────────────────────────────────────────────┘
```

### Next.js テンプレート

| ジョブ | コマンド | 内容 |
|---|---|---|
| Lint & Format | `pnpm run lint` / `pnpm run format:check` | ESLint + Prettier |
| Type Check | `pnpm run typecheck` | `tsc --noEmit` |
| Test | `pnpm run test -- --coverage` | Vitest + カバレッジ |
| Build | `pnpm run build` | Next.js ビルド |
| CD | Docker multi-stage build | `standalone` 出力 → GHCR |

### Spring Boot テンプレート

| ジョブ | コマンド | 内容 |
|---|---|---|
| Lint & Format | `./gradlew spotlessCheck` | Spotless (Google Java Format) |
| Unit Test | `./gradlew test` | JUnit 5 ユニットテスト |
| Integration Test | `./gradlew integrationTest` | PostgreSQL サービスコンテナ付き |
| Build | `./gradlew build -x test` | JAR ビルド（テストスキップ） |
| CD | Docker multi-stage build | Temurin 21 → GHCR |

### 適用されているベストプラクティス

- **Concurrency control** — 同一ブランチの古い実行を自動キャンセル（CI）。CD はキャンセルしない
- **Least privilege** — `permissions: contents: read` を基本とし、CD のみ `packages: write` を追加
- **Caching** — pnpm store / Gradle cache / Docker layer cache (GHA backend)
- **workflow_run trigger** — CD は CI 成功後にのみ起動。CI 失敗時はデプロイされない
- **Dependabot** — GitHub Actions と依存パッケージの自動更新（weekly）
- **Docker multi-stage build** — ビルド成果物のみを最小ランタイムイメージにコピー。非 root ユーザーで実行

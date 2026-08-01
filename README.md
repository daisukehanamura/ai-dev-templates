# ai-dev-templates

AI駆動開発（Claude Code）のためのテンプレートリポジトリ。

## テンプレート

| テンプレート | 説明 | 主な技術 |
|---|---|---|
| `nextjs/` | フロントエンド | Next.js (App Router), TypeScript, ESLint + Prettier |
| `springboot/` | バックエンド | Spring Boot 3.x, Java 21+, Checkstyle + Spotless |

## 使い方

1. このリポジトリをクローン
2. 使いたいテンプレートのディレクトリをプロジェクトにコピー
3. `CLAUDE.md` をプロジェクトに合わせてカスタマイズ

## 構成

```
CLAUDE.md                     # 共通ルール（AI向け）
docs/specs/                   # 仕様書
nextjs/
  CLAUDE.md                   # Next.js 固有ルール
  .claude/commands/            # カスタムコマンド (new-feature)
  .github/workflows/          # CI/CD
  eslint.config.mjs            # ESLint
  .prettierrc.json             # Prettier
springboot/
  CLAUDE.md                   # Spring Boot 固有ルール
  .claude/commands/            # カスタムコマンド (new-endpoint)
  .github/workflows/          # CI/CD
  config/checkstyle/           # Checkstyle
  gradle/lint.gradle.kts       # Spotless
```

## カスタムコマンド

| コマンド | テンプレート | 説明 |
|---|---|---|
| `/new-spec <name>` | 共通 | 会話内容から仕様書を作成 |
| `/new-feature <name>` | Next.js | 機能モジュールの雛形を生成 |
| `/new-endpoint <Name>` | Spring Boot | CRUD エンドポイント一式を生成 |

## 開発プロセス

仕様書 → レビュー → 実装 → 仕様書更新。詳細は `CLAUDE.md` を参照。

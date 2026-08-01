新しい機能モジュールを作成する。

## 引数

$ARGUMENTS に機能名が渡される（例: `auth`, `dashboard`）

## 手順

1. `src/features/$ARGUMENTS/` ディレクトリに以下を作成:
   - `components/` ディレクトリ（.gitkeep）
   - `hooks/` ディレクトリ（.gitkeep）
   - `actions/` ディレクトリ（.gitkeep）
   - `lib/` ディレクトリ（.gitkeep）
   - `types.ts`（基本的な型定義のプレースホルダー）
   - `index.ts`（公開APIのバレルファイル）
2. `src/app/` に対応するルートが必要か確認し、必要であれば作成する
3. 作成した構成をユーザーに報告する

## ルール

- CLAUDE.md のディレクトリ構成と命名規則に従うこと
- 不要なファイルは作らない。最小構成でスタートする

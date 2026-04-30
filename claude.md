# Claude Code - Google Sheets JDBC Agent

あなたは「Java + CData JDBC Driver for Google Sheets」プロジェクトを管理するエージェントです。

## 参照ファイル（必ず確認）
- PROJECT.md: プロジェクトの目的、技術スタック、環境情報
- DESIGN.md: 設計、ディレクトリ構成、フロー
- TASK.md: タスク管理
- SESSION_LOG.md: 作業履歴

---

## Execution Style
- 複雑なタスクは必ずステップ分割する。
- 各ステップごとに「何をするか」を先に提示する。
- ステップ完了後に進捗を報告し、SESSION_LOG.md を更新する。
- 不明点は必ず質問する。

## Working Style
- 既存ファイルを尊重し、小さく変更する。
- Windows と WSL の両方で動作することを前提とする。
- OAuthSettingsLocation の扱いに注意する。

---

## Commands
### 通常実装
「TASK.md の〇〇を実装してください」

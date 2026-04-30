# Google Sheets JDBC 接続成功レポート (success_way.log)

## 1. 前提準備 (Prerequisites)
Google Sheets と JDBC 経由で接続するために、以下の Google Cloud 設定が必要です。

*   **Google Cloud プロジェクトの作成**: [Google Cloud Console](https://console.developers.google.com/) でプロジェクトを作成します。
*   **API の有効化**: 
    *   `Google Sheets API`
    *   `Google Drive API` (ドライバがファイルを検索するために必須)
*   **OAuth クライアント ID の作成**:
    *   種類: 「Web アプリケーション」
    *   承認済みのリダイレクト URI: `http://localhost:33333` (CData デフォルト)
*   **ライブラリ**: `libs/cdata.jdbc.googlesheets.jar` をプロジェクトに配置します。

---

## 2. 発生していた問題 (Problems)
*   **OAuth タイムアウト/403 エラー**: 認証は通るが、その後のスプレッドシート検索で失敗していた。
*   **Invalid Table Name**: 「Products」などのシート名でクエリを投げても「テーブルが見つからない」とエラーになる。
*   **シート一覧が空**: `SELECT * FROM Sheets` を実行しても結果が返ってこない。

---

## 3. 分析結果 (Analysis)
詳細ログ (`cdata_diag.log`) を解析した結果、以下のことが判明しました。

*   **Spreadsheet プロパティの挙動**: ドライバは `Spreadsheet` プロパティに渡された文字列を「ファイル名」として Google Drive 内を検索します。URL を渡すと、URL という名前のファイルを検索してしまうためヒットしません。
*   **テーブル名の命名規則**: CData ドライバは、スプレッドシート内の各シートを `[スプレッドシート名]_[シート名]` という形式のテーブル名として認識します。
    *   例: スプレッドシート名 `Northwind` 、シート名 `Products` の場合、テーブル名は `Northwind_Products` となります。

---

## 4. 解決方法 (Solution)
以下の設定で接続と取得に成功しました。

### 接続文字列のポイント
```
jdbc:googlesheets:
OAuthClientId=[クライアントID];
OAuthClientSecret=[クライアントシークレット];
Spreadsheet=Northwind;  <-- IDやURLではなく「ファイル名」を指定
InitiateOAuth=GETANDREFRESH;
OAuthSettingsLocation=C:\...\OAuthSettings.xml;
```

### クエリのポイント
システムテーブルを検索して、ドライバが認識している正確なテーブル名を確認することが重要です。
```sql
-- 認識されているテーブル名を確認
SELECT TableName FROM sys_tables;

-- 実際のデータ取得 (プレフィックスが必要)
SELECT * FROM Northwind_Products;
```

---

## 5. 動作確認済み環境
*   Java: 21.0.11
*   Driver: CData JDBC Driver for Google Sheets 2025 (25.0.9540.0)
*   検証用クラス: `Diagnostic.java` (このファイルのみで接続・一覧・取得の全工程を確認可能)

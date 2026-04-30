# Google Sheets 接続用 Java プロジェクト

Google スプレッドシートに CData JDBC Driver を使用して接続するためのサンプルコードです。

## 概要

本プロジェクトは、`Diagnostic.java` を実行することで Google スプレッドシートのデータを Java アプリケーションから取得することを目的としています。

## ディレクトリ構成

```
test_driver/
├── Diagnostic.java              # 接続・一覧表示・データ取得を行うメインプログラム
├── GEMINI.md                    # 開発時のやり取りをまとめたレポート
├── README.md                    # プロジェクト説明（このファイル）
├── success_way.log              # 接続確立までの手順と解決策のまとめ
├── cdata_diag.log               # CData JDBC Driver の動作ログ（エラー詳細）
├── .gitignore                   # バージョン管理から除外するファイル
├── .agent/                      # エージェント設定ディレクトリ
└── libs/
    └── cdata.jdbc.googlesheets.jar # CData JDBC Driver
```

## 事前準備

**1. Google Cloud プロジェクトの設定**

   - Google Cloud Console で API を有効化する必要があります。
     - [OAuth 2.0 クライアント ID](https://qiita.com/cdata_japan/items/71642f0352663834d62a) の作成
     - [Google Sheets API](https://www.cdata.com/jp/help/google-sheets-driver/help/jdbc/install-api-access.htm) の有効化
     - [Google Sheets OAuth 認証](https://www.cdata.com/jp/help/google-sheets-driver/help/jdbc/getting-started.htm) の設定

   - クライアント ID とクライアントシークレットを取得します。

**2. CData JDBC Driver の導入**

   - [CData JDBC Driver for Google Sheets](https://www.cdata.com/jp/googlesheets/jdbc/) をダウンロードします。
   - 展開したフォルダから `cdata.jdbc.googlesheets.jar` をプロジェクトの `libs/` ディレクトリにコピーします。

## 接続方法

`Diagnostic.java` を実行する際に、以下の情報を正しく設定する必要があります。

- `clientId`: Google Cloud のクライアント ID
- `clientSecret`: Google Cloud のクライアントシークレット
- `spreadsheetName`: 接続先の Google スプレッドシートの名前（完全一致が必要）
- `settingsPath`: `OAuthSettings_diag.xml` へのパス

## 実行方法

1. **コンパイル**

   ```bash
   javac -cp ".;libs\cdata.jdbc.googlesheets.jar" Diagnostic.java
   ```

2. **実行**

   ```bash
   java -cp ".;libs\cdata.jdbc.googlesheets.jar" Diagnostic
   ```

## 正常に接続できた場合の出力

```
Connecting to Google Sheets...
Target Spreadsheet: Northwind
SUCCESS: Connected!

--- Available Tables (sys_tables) ---
Table: Sheets (VIEW)
Table: Spreadsheets (VIEW)

--- Sheets in this Spreadsheet ---
Sheet: Products (Rows: 1000)
Sheet: Categories (Rows: 1000)
Sheet: RSSBUSWORKSHEETRSSBUS0 (Rows: 1000)
Sheet: Suppliers (Rows: 1000)
Sheet: Orders (Rows: 1182)

--- Data from Northwind_Products ---
1 | Chai | 18
2 | Chang | 19
3 | Aniseed Syrup | 10
4 | Chef Anton's Cajun Seasoning | 22
5 | Chef Anton's Gumbo Mix | 21.35
```

## トラブルシューティング

接続できない場合は、`cdata_diag.log` に詳細なエラーログが出力されます。
一般的な問題と解決策は以下の通りです：

- **エラー: "Spreadsheet not found"**
  - スプレッドシート名が完全一致しているか確認してください（スペースや大文字・小文字を含む）。
  - API が有効になっているか、OAuth の設定が正しいか確認してください。

- **エラー: "Google Sheets OAuth Authentication is required"**
  - `InitiateOAuth=GETANDREFRESH;` が設定されているか確認してください。
  - 初回実行時にブラウザで認証が必要になる場合があります。

## ライセンス

- CData JDBC Driver: [CData](https://www.cdata.com/)
- 本プロジェクトのサンプルコード: MIT
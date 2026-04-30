# 次ステップ実行ガイド（A要件を最速でGUI化する）

このドキュメントは、**課題Aのみ**を最短でWebアプリ化するための作業メモです。  
（後で仕様強化・セキュリティ強化する前提の、アジャイルな最小版）

---

## 0. ゴール（今回）

- 画面で `country` を入力して検索できる
- `usa` と `USA` を同じとして扱う
- Google Sheets から、該当国のSupplierが扱うProduct一覧を表示できる

---

## 1. Spring Initializr 設定（これで作成）

はい、まずは **あなたがSpring Initializrで雛形作成** でOKです。  
設定は以下を推奨します（最小構成）。

### Project
- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 4.0.6（画面にある安定版でOK）

### Project Metadata（例）
- **Group**: `com.example`
- **Artifact**: `northwind-app`
- **Name**: `northwind-app`
- **Package name**: `com.example.northwindapp`
- **Packaging**: Jar
- **Java**: 21

### Dependencies（最小）
- **Spring Web**
- **Thymeleaf**

> 補足: まずは最短で動かすため、`Spring Data JDBC` は必須ではありません。  
> CDataドライバを使って `java.sql` で直接接続します。

---

## 2. 雛形展開後の最低ファイル構成

以下だけ作れば一旦動きます。

- `src/main/java/.../controller/ProductController.java`
- `src/main/java/.../service/ProductQueryService.java`
- `src/main/java/.../model/ProductRow.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/application.yml`

---

## 3. 実装ポリシー（今回だけの割り切り）

- SQLの責務分離は**最小限**（ServiceにSQL直書きでOK）
- 例外処理は**簡単**（画面にエラーメッセージを表示）
- セキュリティは**最低限**
  - `clientSecret` はコード直書きしない
  - `application.yml` + 環境変数参照にする

---

## 4. API/画面仕様（Aだけ）

### 画面
- `GET /` で検索画面表示
- 入力: `country`
- ボタン: `検索`
- 結果: 表形式

### 動作
- 空白は `trim` してから検索
- 大文字小文字は区別しない
  - 実装例: `WHERE UPPER(TRIM(s.Country)) = UPPER(TRIM(?))`

---

## 5. 接続情報の管理（最低限）

`application.yml` ではプレースホルダで定義し、値は環境変数から読みます。

```yaml
app:
  sheets:
    client-id: ${GS_CLIENT_ID:}
    client-secret: ${GS_CLIENT_SECRET:}
    spreadsheet: ${GS_SPREADSHEET:Northwind}
    oauth-settings-location: ${GS_OAUTH_SETTINGS:}
```

JDBC URLはService内で組み立て。

---

## 6. 最小SQL（A要件）

```sql
SELECT p.ProductID, p.ProductName, c.CategoryName,
       p.UnitPrice, s.CompanyName, s.Country
FROM Northwind_Products p
INNER JOIN Northwind_Suppliers s ON p.SupplierID = s.SupplierID
INNER JOIN Northwind_Categories c ON p.CategoryID = c.CategoryID
WHERE UPPER(TRIM(s.Country)) = UPPER(TRIM(?))
ORDER BY p.ProductName
```

---

## 7. 完了条件（Definition of Done）

- `http://localhost:8080` を開ける
- `USA` または `usa` で同じ結果が出る
- 一覧に Product/Supplier/Country が表示される
- エラー時に画面へ簡易メッセージが出る

---

## 8. 次フェーズ（あとでやる）

- B要件: `category`, `minPrice` 追加
- C要件: 国別平均注文価格 + ソート
- セキュリティ強化（Secrets管理、ログ方針、入力検証）
- 責務分離（Repository層導入）

# Third Step 議事録: Northwind Web アプリ雛形作成

作成日時: 2026-04-30 16:55 JST

## 1. 目的

`nextStep.md` に記載された A 要件を、Spring Initializr で作成済みの `C:\StudySpace\test_driver\northwind-app` を使って Web アプリ化した。

今回の主目的は次の通り。

- `GET /` で検索画面を表示する。
- 画面から `country` を入力して検索できる。
- `USA` と `usa` を同じ検索条件として扱う。
- Google Sheets 上の Northwind データから、該当国の Supplier に紐づく Product 一覧を表示する。
- 一覧には少なくとも Product / Supplier / Country を表示する。
- 接続エラー時は画面に簡単なエラーメッセージを表示する。
- `clientSecret` などの認証情報をコードに直書きしない。
- TDD 方針で、サービスとコントローラの期待動作をテストで固定する。

## 2. 使用したプロジェクト

Spring Initializr で準備済みの Maven プロジェクトを使用した。

```text
C:\StudySpace\test_driver\northwind-app\northwind-app
```

主な前提は次の通り。

- Java: 21
- Spring Boot: 4.0.6
- Build: Maven
- Dependencies: Spring Web MVC, Thymeleaf
- Package: `com.example.northwind_app`
- CData JDBC Driver: `C:\StudySpace\test_driver\libs\cdata.jdbc.googlesheets.jar`

## 3. 実装後のディレクトリ構成

主要ファイルのみ抜粋。

```text
northwind-app/northwind-app
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/example/northwind_app
│   │   │       ├── NorthwindAppApplication.java
│   │   │       ├── config
│   │   │       │   └── SheetsProperties.java
│   │   │       ├── controller
│   │   │       │   └── ProductController.java
│   │   │       ├── model
│   │   │       │   └── ProductRow.java
│   │   │       └── service
│   │   │           ├── ConnectionFactory.java
│   │   │           ├── DriverManagerConnectionFactory.java
│   │   │           └── ProductQueryService.java
│   │   └── resources
│   │       ├── application.yml
│   │       └── templates
│   │           └── index.html
│   └── test
│       └── java
│           └── com/example/northwind_app
│               ├── NorthwindAppApplicationTests.java
│               ├── controller
│               │   └── ProductControllerTest.java
│               └── service
│                   └── ProductQueryServiceTest.java
```

## 4. 実装したファイルと内容

### 4.1 `pom.xml`

CData Google Sheets JDBC Driver を Maven ビルドに含めるため、`system` scope の依存関係を追加した。

```xml
<dependency>
  <groupId>cdata</groupId>
  <artifactId>googlesheets-jdbc</artifactId>
  <version>1.0</version>
  <scope>system</scope>
  <systemPath>${project.basedir}/../../libs/cdata.jdbc.googlesheets.jar</systemPath>
</dependency>
```

また、Spring Boot の実行 jar に system scope の jar を含めるため、`spring-boot-maven-plugin` に次を追加した。

```xml
<includeSystemScope>true</includeSystemScope>
```

補足:

- Maven からは system scope に対する警告が出る。
- 今回は課題の最短達成を優先し、ローカルの CData jar を直接参照した。
- 本格運用では社内 Maven リポジトリ、またはローカル Maven リポジトリへの install 方式に移行するのが望ましい。

### 4.2 `NorthwindAppApplication.java`

設定クラスを `application.yml` からバインドできるように、`@ConfigurationPropertiesScan` を追加した。

役割:

- Spring Boot アプリケーションのエントリポイント。
- `SheetsProperties` を自動検出する。

### 4.3 `config/SheetsProperties.java`

Google Sheets 接続に必要な設定値を受け取る record。

保持する値:

- `clientId`
- `clientSecret`
- `spreadsheet`
- `oauthSettingsLocation`

`@ConfigurationProperties(prefix = "app.sheets")` を付与し、`application.yml` の `app.sheets.*` から値を受け取る。

### 4.4 `model/ProductRow.java`

検索結果 1 行分を表す record。

フィールド:

- `productId`
- `productName`
- `categoryName`
- `unitPrice`
- `supplierName`
- `country`

画面表示とサービス戻り値の型を明確にするために追加した。

### 4.5 `service/ConnectionFactory.java`

JDBC 接続生成を差し替えるための小さなインターフェース。

```java
Connection open(String jdbcUrl) throws SQLException;
```

目的:

- 本番では `DriverManager` で CData JDBC に接続する。
- テストでは実 DB / Google Sheets に接続せず、Connection をモックまたは Proxy で差し替える。
- `ProductQueryService` の SQL とマッピングを TDD しやすくする。

### 4.6 `service/DriverManagerConnectionFactory.java`

本番用の JDBC 接続生成クラス。

処理内容:

- `Class.forName("cdata.jdbc.googlesheets.GoogleSheetsDriver")` で CData Driver をロードする。
- `DriverManager.getConnection(jdbcUrl)` で接続を開く。
- Driver が見つからない場合は `SQLException` として扱う。

### 4.7 `service/ProductQueryService.java`

Northwind の Product 検索を担当するサービス。

主な処理:

- 入力された `country` を trim する。
- 空文字の場合は DB 接続せず、空リストを返す。
- CData Google Sheets 用 JDBC URL を組み立てる。
- `PreparedStatement` で SQL を実行する。
- `ResultSet` を `ProductRow` にマッピングする。

実装した SQL:

```sql
SELECT p.ProductID, p.ProductName, c.CategoryName,
       p.UnitPrice, s.CompanyName, s.Country
FROM Northwind_Products p
INNER JOIN Northwind_Suppliers s ON p.SupplierID = s.SupplierID
INNER JOIN Northwind_Categories c ON p.CategoryID = c.CategoryID
WHERE UPPER(TRIM(s.Country)) = UPPER(TRIM(?))
ORDER BY p.ProductName
```

重要点:

- `UPPER(TRIM(s.Country)) = UPPER(TRIM(?))` により、DB 側でも大小文字と前後空白を吸収する。
- SQL インジェクション対策として文字列結合ではなく `PreparedStatement` を使用した。
- `clientSecret` はコード内に書かず、設定から受け取る。

### 4.8 `controller/ProductController.java`

`GET /` を担当する MVC コントローラ。

動作:

- `country` 未指定の場合は検索フォームだけ表示する。
- `country` 指定時は前後空白を trim する。
- `ProductQueryService.findByCountry()` を呼び出す。
- 成功時は `products` を Model に追加する。
- `SQLException` 発生時は `errorMessage` を Model に追加する。

エラーメッセージ:

```text
検索中にエラーが発生しました。接続設定を確認してください。
```

### 4.9 `resources/application.yml`

既存の `application.properties` を削除し、YAML に置き換えた。

内容:

```yaml
spring:
  application:
    name: northwind-app

app:
  sheets:
    client-id: ${GS_CLIENT_ID:}
    client-secret: ${GS_CLIENT_SECRET:}
    spreadsheet: ${GS_SPREADSHEET:Northwind}
    oauth-settings-location: ${GS_OAUTH_SETTINGS:}
```

設定方針:

- `GS_CLIENT_ID`
- `GS_CLIENT_SECRET`
- `GS_SPREADSHEET`
- `GS_OAUTH_SETTINGS`

上記の環境変数から値を受け取る。

`GS_SPREADSHEET` は未指定時に `Northwind` を使う。

### 4.10 `resources/templates/index.html`

Thymeleaf の検索画面。

実装内容:

- `Country` 入力欄
- `検索` ボタン
- エラー時のメッセージ表示
- 検索結果テーブル
- 結果 0 件時のメッセージ

表示列:

- Product
- Category
- Price
- Supplier
- Country

要件では Product / Supplier / Country が必須だったが、SQL で取得している Category / Price も実用上有用なため表示した。

## 5. テスト内容

TDD 方針で、外部の Google Sheets に依存しないテストを追加した。

### 5.1 `controller/ProductControllerTest.java`

コントローラ単体テスト。

#### テスト 1: `trimsCountryAndDisplaysProducts`

確認したこと:

- 入力 `"  usa  "` が `"usa"` に trim される。
- trim 後の値で `ProductQueryService.findByCountry("usa")` が呼ばれる。
- Service の戻り値が `products` として Model に入る。
- View 名が `index` になる。

#### テスト 2: `displaysSimpleErrorMessageWhenQueryFails`

確認したこと:

- Service が `SQLException` を投げた場合でも Controller が例外を画面外に漏らさない。
- `errorMessage` が Model に入る。
- `products` は空リストのままになる。

### 5.2 `service/ProductQueryServiceTest.java`

サービス単体テスト。

実 DB に接続せず、`ConnectionFactory` を差し替えて JDBC オブジェクトを Proxy で用意した。

#### テスト 1: `searchesByTrimmedCountryUsingCaseInsensitiveSql`

確認したこと:

- SQL に `UPPER(TRIM(s.Country)) = UPPER(TRIM(?))` が含まれる。
- SQL に `ORDER BY p.ProductName` が含まれる。
- 入力 `"  usa  "` が `"usa"` として `PreparedStatement` にセットされる。
- JDBC URL に次の値が含まれる。
  - `OAuthClientId=client;`
  - `OAuthClientSecret=secret;`
  - `Spreadsheet=Northwind;`
  - `InitiateOAuth=GETANDREFRESH;`
  - `OAuthSettingsLocation=C:\oauth.xml;`
- `ResultSet` の値が `ProductRow` に正しくマッピングされる。

#### テスト 2: `blankCountryReturnsNoRowsWithoutOpeningConnection`

確認したこと:

- 空白だけの country は空リストを返す。
- 空白入力時は JDBC 接続を開かない。

### 5.3 `NorthwindAppApplicationTests.java`

Spring Initializr が生成したコンテキストロードテスト。

確認したこと:

- Spring Boot アプリケーションコンテキストが起動する。
- `@ConfigurationPropertiesScan`、Controller、Service、Properties の Bean 構成に破綻がない。

## 6. 実行・検証結果

### 6.1 Maven テスト

実行コマンド:

```powershell
mvn test
```

結果:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 6.2 アプリ起動

実行コマンド:

```powershell
mvn spring-boot:run
```

起動確認:

```text
Tomcat started on port 8080 (http) with context path '/'
Started NorthwindAppApplication
```

確認 URL:

```text
http://localhost:8080/
```

HTTP 確認:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/
```

結果:

```text
StatusCode: 200
```

### 6.3 `USA` / `usa` の同一結果確認

確認 URL:

```text
http://localhost:8080/?country=USA
http://localhost:8080/?country=usa
```

検証結果:

```text
UpperProductCells: 60
LowerProductCells: 60
SameContent: True
```

つまり、`USA` と `usa` で同じ検索結果が返ることを確認した。

### 6.4 実データ表示確認

`country=USA` で取得できた表示例:

```text
Boston Crab Meat | Seafood | 18 | New England Seafood Cannery | USA
Chef Anton's Cajun Seasoning | Condiments | 22 | New Orleans Cajun Delights | USA
Chef Anton's Gumbo Mix | Condiments | 21 | New Orleans Cajun Delights | USA
```

Product / Supplier / Country の表示要件を満たしている。

## 7. 苦戦した障害・対応

### 7.1 `nextStep.md` の文字化け

`nextStep.md` は文字化けしていたが、要件の主要部分は読み取れた。

読み取れた内容:

- `country` で検索する。
- `usa` と `USA` を同一扱いにする。
- Google Sheets の Northwind データを使用する。
- Product / Supplier / Country を一覧表示する。
- エラー時に簡単なメッセージを画面表示する。
- `application.yml` と環境変数で認証情報を扱う。

対応:

- 文字化け部分を無理に復元せず、読める要件に基づいて最小構成で実装した。

### 7.2 Maven Wrapper が PowerShell 上で起動失敗

最初に次を実行した。

```powershell
.\mvnw.cmd test
```

しかし、Wrapper が次のエラーで失敗した。

```text
Cannot index into a null array.
Cannot start maven from wrapper
```

対応:

- 端末にインストール済みの Maven を確認した。
- `mvn -v` で Maven 3.9.15 / Java 21 を確認できた。
- 以降は `mvn test` と `mvn spring-boot:run` を使用した。

### 7.3 サンドボックス環境で Maven ローカルリポジトリにアクセスできない

通常実行では次のエラーが出た。

```text
Could not create local repository at C:\Users\CodexSandboxOffline\.m2\repository
```

対応:

- ユーザープロファイル側の Maven ローカルリポジトリを使う必要があったため、権限昇格付きで `mvn test` を実行した。
- その後、依存関係のダウンロードとテスト実行が成功した。

### 7.4 CData JDBC Driver の Maven 取り込み

CData Driver は Maven Central から取得する依存関係ではなく、ローカル jar として存在していた。

対応:

- `pom.xml` に `systemPath` で `../../libs/cdata.jdbc.googlesheets.jar` を指定した。
- Spring Boot 実行時にも含めるため `includeSystemScope` を設定した。

残課題:

- Maven の警告が出る。
- 本格運用では `mvn install:install-file` または社内 Maven リポジトリ管理に変更した方がよい。

### 7.5 外部 Google Sheets に依存しない TDD

実際の Google Sheets 接続をテストで毎回行うと、認証・ネットワーク・データ状態に左右される。

対応:

- `ConnectionFactory` を導入した。
- Service テストでは JDBC の `Connection` / `PreparedStatement` / `ResultSet` を Proxy で差し替えた。
- SQL、パラメータ、ResultSet マッピングだけを安定して検証できるようにした。

## 8. 現在の達成状況

完了条件に対する状況。

| 条件 | 状況 |
| --- | --- |
| `http://localhost:8080` を開ける | 達成 |
| `USA` と `usa` で同じ結果が出る | 達成 |
| Product / Supplier / Country が表示される | 達成 |
| エラー時に簡単なメッセージが表示される | 実装済み、単体テスト済み |
| 認証情報をコードに直書きしない | 達成 |
| TDD による検証 | 達成 |

## 9. 注意事項

### 9.1 認証情報

アプリ実行時は環境変数が必要。

```powershell
$env:GS_CLIENT_ID="..."
$env:GS_CLIENT_SECRET="..."
$env:GS_SPREADSHEET="Northwind"
$env:GS_OAUTH_SETTINGS="C:\StudySpace\test_driver\OAuthSettings_diag.xml"
```

`GS_SPREADSHEET` は未設定でも `Northwind` が使われる。

### 9.2 現在の git 状態

作業後の `git status --short` では、今回作成した `northwind-app/` 以外にも既存変更が見えていた。

```text
MM Diagnostic.java
 D success_way.md
?? nextStep.md
?? northwind-app/
```

`Diagnostic.java` や `success_way.md` は今回の Web アプリ実装では編集していないため、扱いには注意する。

## 10. 今後の展望

### 10.1 B 要件: 検索条件追加

次フェーズでは次の検索条件を追加する予定。

- `category`
- `minPrice`

想定対応:

- `ProductSearchCondition` のような検索条件 record を追加する。
- SQL の WHERE 条件を動的に組み立てる。
- Controller の request parameter を増やす。
- 画面に Category 入力欄と Min Price 入力欄を追加する。
- Service テストで条件の有無に応じた SQL と parameter 順を検証する。

### 10.2 C 要件: 集計・ソート

候補:

- 国別平均注文価格
- ProductName / UnitPrice / SupplierName によるソート
- 件数表示

想定対応:

- SQL を分けるか、画面内に集計用 Service メソッドを追加する。
- ソート対象はホワイトリスト方式にして、SQL インジェクションを避ける。

### 10.3 セキュリティ強化

現在は最小限の安全策のみ。

今後の候補:

- secret 管理を `.env` ではなく OS Secret Manager / CI Secret に寄せる。
- エラーログには詳細を残し、画面には一般化したメッセージだけを出す。
- 入力値の長さ制限を追加する。
- 接続タイムアウトやリトライ方針を明確にする。

### 10.4 Repository 層の導入

現状は最短実装として `ProductQueryService` に SQL を直接置いている。

今後の候補:

- `ProductRepository` を追加する。
- Service は入力整形・業務判断、Repository は SQL 実行に分離する。
- テストも Service と Repository に分ける。

### 10.5 CData Driver の依存管理改善

現在の `systemPath` 方式は簡便だが、Maven 的には推奨されない。

改善案:

```powershell
mvn install:install-file `
  -Dfile=C:\StudySpace\test_driver\libs\cdata.jdbc.googlesheets.jar `
  -DgroupId=cdata `
  -DartifactId=googlesheets-jdbc `
  -Dversion=1.0 `
  -Dpackaging=jar
```

その後、`pom.xml` では通常の dependency として参照する。

### 10.6 E2E テストの追加

現在は単体テストと手動 HTTP 確認が中心。

今後の候補:

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` で Controller から HTML まで確認する。
- Playwright 等でブラウザ操作を自動化する。
- CData 接続はプロファイルを分け、本番接続テストと通常 CI テストを分離する。

## 11. 次回作業メモ

次に着手するなら、優先順位は次の通り。

1. `ProductRepository` 分離
2. `category` / `minPrice` 条件追加
3. 画面の検索フォーム拡張
4. ソートと集計
5. CData jar の Maven 管理改善
6. E2E テスト導入

現時点では A 要件の最小 Web アプリとして動作確認済み。

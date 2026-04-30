import java.sql.*;

public class Diagnostic {
    public static void main(String[] args) {
        // 1. 接続情報の準備
        String clientId = "YOUR_CLIENT_ID";
        String clientSecret = "YOUR_CLIENT_SECRET";
        
        // ログから判明した正しい「名前」を指定
        String spreadsheetName = "Northwind";
        String settingsPath = "C:\\StudySpace\\test_driver\\OAuthSettings_diag.xml";

        String url = "jdbc:googlesheets:"
                + "OAuthClientId=" + clientId + ";"
                + "OAuthClientSecret=" + clientSecret + ";"
                + "Spreadsheet=" + spreadsheetName + ";"
                + "InitiateOAuth=GETANDREFRESH;"
                + "OAuthSettingsLocation=" + settingsPath + ";"
                + "Logfile=C:\\StudySpace\\test_driver\\cdata_diag.log;"
                + "Verbosity=3;";

        try {
            Class.forName("cdata.jdbc.googlesheets.GoogleSheetsDriver");
            System.out.println("Connecting to Google Sheets...");
            System.out.println("Target Spreadsheet: " + spreadsheetName);
            
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("SUCCESS: Connected!");

                // 2. テーブル一覧の確認
                System.out.println("\n--- Available Tables (sys_tables) ---");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT TableName, TableType FROM sys_tables")) {
                    while (rs.next()) {
                        System.out.println("Table: " + rs.getString("TableName") + " (" + rs.getString("TableType") + ")");
                    }
                }

                // 4. 実際のデータ取得テスト
                System.out.println("\n--- Data from Northwind_Products ---");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT ProductID, ProductName, UnitPrice FROM Northwind_Products LIMIT 5")) {
                    while (rs.next()) {
                        System.out.println(rs.getString("ProductID") + " | " + rs.getString("ProductName") + " | " + rs.getString("UnitPrice"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("\nFAILURE: An error occurred.");
            e.printStackTrace();
        }
    }
}

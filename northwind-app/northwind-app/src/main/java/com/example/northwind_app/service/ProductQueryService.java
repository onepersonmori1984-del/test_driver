package com.example.northwind_app.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.northwind_app.config.SheetsProperties;
import com.example.northwind_app.model.ProductRow;

@Service
public class ProductQueryService {

	static final String PRODUCT_BY_COUNTRY_SQL = """
			SELECT p.ProductID, p.ProductName, c.CategoryName,
			       p.UnitPrice, s.CompanyName, s.Country
			FROM Northwind_Products p
			INNER JOIN Northwind_Suppliers s ON p.SupplierID = s.SupplierID
			INNER JOIN Northwind_Categories c ON p.CategoryID = c.CategoryID
			WHERE UPPER(TRIM(s.Country)) = UPPER(TRIM(?))
			ORDER BY p.ProductName
			""";

	private final SheetsProperties properties;
	private final ConnectionFactory connectionFactory;

	public ProductQueryService(SheetsProperties properties, ConnectionFactory connectionFactory) {
		this.properties = properties;
		this.connectionFactory = connectionFactory;
	}

	public List<ProductRow> findByCountry(String country) throws SQLException {
		String trimmedCountry = country == null ? "" : country.trim();
		if (trimmedCountry.isEmpty()) {
			return List.of();
		}

		try (Connection connection = connectionFactory.open(jdbcUrl());
				PreparedStatement statement = connection.prepareStatement(PRODUCT_BY_COUNTRY_SQL)) {
			statement.setString(1, trimmedCountry);

			try (ResultSet resultSet = statement.executeQuery()) {
				List<ProductRow> rows = new ArrayList<>();
				while (resultSet.next()) {
					rows.add(toProductRow(resultSet));
				}
				return rows;
			}
		}
	}

	String jdbcUrl() {
		StringBuilder url = new StringBuilder("jdbc:googlesheets:");
		appendJdbcOption(url, "OAuthClientId", properties.clientId());
		appendJdbcOption(url, "OAuthClientSecret", properties.clientSecret());
		appendJdbcOption(url, "Spreadsheet", properties.spreadsheet());
		url.append("InitiateOAuth=GETANDREFRESH;");
		appendJdbcOption(url, "OAuthSettingsLocation", properties.oauthSettingsLocation());
		return url.toString();
	}

	private static ProductRow toProductRow(ResultSet resultSet) throws SQLException {
		return new ProductRow(
				resultSet.getInt("ProductID"),
				resultSet.getString("ProductName"),
				resultSet.getString("CategoryName"),
				resultSet.getBigDecimal("UnitPrice"),
				resultSet.getString("CompanyName"),
				resultSet.getString("Country"));
	}

	private static void appendJdbcOption(StringBuilder url, String key, String value) {
		if (value != null && !value.isBlank()) {
			url.append(key).append("=").append(value.trim()).append(";");
		}
	}
}

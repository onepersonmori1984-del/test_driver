package com.example.northwind_app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.example.northwind_app.config.SheetsProperties;
import com.example.northwind_app.model.ProductRow;

class ProductQueryServiceTest {

	@Test
	void searchesByTrimmedCountryUsingCaseInsensitiveSql() throws Exception {
		AtomicReference<String> jdbcUrl = new AtomicReference<>();
		AtomicReference<String> sql = new AtomicReference<>();
		AtomicReference<String> parameter = new AtomicReference<>();
		List<Map<String, Object>> data = List.of(Map.of(
				"ProductID", 1,
				"ProductName", "Chai",
				"CategoryName", "Beverages",
				"UnitPrice", new BigDecimal("18.00"),
				"CompanyName", "Exotic Liquids",
				"Country", "USA"));
		ProductQueryService sut = new ProductQueryService(
				new SheetsProperties("client", "secret", "Northwind", "C:\\oauth.xml"),
				url -> {
					jdbcUrl.set(url);
					return connection(sql, parameter, data);
				});

		List<ProductRow> rows = sut.findByCountry("  usa  ");

		assertThat(sql.get()).contains("UPPER(TRIM(s.Country)) = UPPER(TRIM(?))");
		assertThat(sql.get()).contains("ORDER BY p.ProductName");
		assertThat(parameter.get()).isEqualTo("usa");
		assertThat(jdbcUrl.get()).contains("OAuthClientId=client;");
		assertThat(jdbcUrl.get()).contains("OAuthClientSecret=secret;");
		assertThat(jdbcUrl.get()).contains("Spreadsheet=Northwind;");
		assertThat(jdbcUrl.get()).contains("InitiateOAuth=GETANDREFRESH;");
		assertThat(jdbcUrl.get()).contains("OAuthSettingsLocation=C:\\oauth.xml;");
		assertThat(rows).containsExactly(new ProductRow(1, "Chai", "Beverages", new BigDecimal("18.00"), "Exotic Liquids", "USA"));
	}

	@Test
	void blankCountryReturnsNoRowsWithoutOpeningConnection() throws Exception {
		AtomicInteger openCount = new AtomicInteger();
		ProductQueryService sut = new ProductQueryService(
				new SheetsProperties("", "", "Northwind", ""),
				url -> {
					openCount.incrementAndGet();
					throw new AssertionError("connection should not be opened");
				});

		assertThat(sut.findByCountry("   ")).isEmpty();
		assertThat(openCount).hasValue(0);
	}

	private static Connection connection(AtomicReference<String> sql, AtomicReference<String> parameter, List<Map<String, Object>> data) {
		InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
			case "prepareStatement" -> {
				sql.set((String) args[0]);
				yield preparedStatement(parameter, data);
			}
			case "close" -> null;
			case "isClosed" -> false;
			default -> throw new UnsupportedOperationException(method.getName());
		};
		return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, handler);
	}

	private static PreparedStatement preparedStatement(AtomicReference<String> parameter, List<Map<String, Object>> data) {
		InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
			case "setString" -> {
				parameter.set((String) args[1]);
				yield null;
			}
			case "executeQuery" -> resultSet(data);
			case "close" -> null;
			default -> throw new UnsupportedOperationException(method.getName());
		};
		return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, handler);
	}

	private static ResultSet resultSet(List<Map<String, Object>> data) {
		AtomicInteger index = new AtomicInteger(-1);
		InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
			case "next" -> index.incrementAndGet() < data.size();
			case "getInt" -> data.get(index.get()).get((String) args[0]);
			case "getString" -> data.get(index.get()).get((String) args[0]);
			case "getBigDecimal" -> data.get(index.get()).get((String) args[0]);
			case "close" -> null;
			default -> throw new UnsupportedOperationException(method.getName());
		};
		return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[] { ResultSet.class }, handler);
	}
}

package com.example.northwind_app.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

@Component
public class DriverManagerConnectionFactory implements ConnectionFactory {

	@Override
	public Connection open(String jdbcUrl) throws SQLException {
		try {
			Class.forName("cdata.jdbc.googlesheets.GoogleSheetsDriver");
		}
		catch (ClassNotFoundException ex) {
			throw new SQLException("Google Sheets JDBC driver was not found.", ex);
		}
		return DriverManager.getConnection(jdbcUrl);
	}
}

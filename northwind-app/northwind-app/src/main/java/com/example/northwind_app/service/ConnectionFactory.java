package com.example.northwind_app.service;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionFactory {
	Connection open(String jdbcUrl) throws SQLException;
}

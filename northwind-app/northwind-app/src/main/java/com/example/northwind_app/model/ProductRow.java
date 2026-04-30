package com.example.northwind_app.model;

import java.math.BigDecimal;

public record ProductRow(
		int productId,
		String productName,
		String categoryName,
		BigDecimal unitPrice,
		String supplierName,
		String country) {
}

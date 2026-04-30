package com.example.northwind_app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.example.northwind_app.model.ProductRow;
import com.example.northwind_app.service.ProductQueryService;

class ProductControllerTest {

	private final ProductQueryService service = org.mockito.Mockito.mock(ProductQueryService.class);
	private final ProductController sut = new ProductController(service);

	@Test
	void trimsCountryAndDisplaysProducts() throws Exception {
		List<ProductRow> rows = List.of(new ProductRow(1, "Chai", "Beverages", new BigDecimal("18.00"), "Exotic Liquids", "USA"));
		when(service.findByCountry("usa")).thenReturn(rows);
		ExtendedModelMap model = new ExtendedModelMap();

		String view = sut.index("  usa  ", model);

		assertThat(view).isEqualTo("index");
		assertThat(model.get("country")).isEqualTo("usa");
		assertThat(model.get("products")).isEqualTo(rows);
		assertThat((Boolean) model.get("showEmptyMessage")).isFalse();
		verify(service).findByCountry("usa");
	}

	@Test
	void showsEmptyMessageWhenNoProductsAreFound() throws Exception {
		when(service.findByCountry("USA")).thenReturn(List.of());
		ExtendedModelMap model = new ExtendedModelMap();

		sut.index("USA", model);

		assertThat(model.get("products")).isEqualTo(List.of());
		assertThat((Boolean) model.get("showEmptyMessage")).isTrue();
	}

	@Test
	void displaysSimpleErrorMessageWhenQueryFails() throws Exception {
		when(service.findByCountry("USA")).thenThrow(new SQLException("boom"));
		ExtendedModelMap model = new ExtendedModelMap();

		sut.index("USA", model);

		assertThat(model.get("errorMessage")).isEqualTo("検索中にエラーが発生しました。接続設定を確認してください。");
		assertThat(model.get("products")).isEqualTo(List.of());
		assertThat((Boolean) model.get("showEmptyMessage")).isFalse();
	}
}

package com.example.northwind_app.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.northwind_app.model.ProductRow;
import com.example.northwind_app.service.ProductQueryService;

@Controller
public class ProductController {

	private final ProductQueryService productQueryService;

	public ProductController(ProductQueryService productQueryService) {
		this.productQueryService = productQueryService;
	}

	@GetMapping("/")
	public String index(@RequestParam(name = "country", required = false) String country, Model model) {
		String searchCountry = country == null ? "" : country.trim();
		List<ProductRow> products = List.of();
		boolean showEmptyMessage = false;
		String errorMessage = null;

		if (!searchCountry.isEmpty()) {
			try {
				products = productQueryService.findByCountry(searchCountry);
				showEmptyMessage = products.isEmpty();
			}
			catch (SQLException ex) {
				errorMessage = "検索中にエラーが発生しました。接続設定を確認してください。";
			}
		}

		model.addAttribute("country", searchCountry);
		model.addAttribute("products", products);
		model.addAttribute("showEmptyMessage", showEmptyMessage);
		model.addAttribute("errorMessage", errorMessage);
		return "index";
	}
}

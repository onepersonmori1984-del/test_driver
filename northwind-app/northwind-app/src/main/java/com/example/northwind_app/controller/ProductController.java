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
		String trimmedCountry = country == null ? "" : country.trim();
		model.addAttribute("country", trimmedCountry);
		model.addAttribute("products", List.of());

		if (trimmedCountry.isEmpty()) {
			return "index";
		}

		try {
			model.addAttribute("products", productQueryService.findByCountry(trimmedCountry));
		}
		catch (SQLException ex) {
			model.addAttribute("errorMessage", "検索中にエラーが発生しました。接続設定を確認してください。");
		}
		return "index";
	}
}

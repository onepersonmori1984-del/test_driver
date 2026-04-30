package com.example.northwind_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sheets")
public record SheetsProperties(
		String clientId,
		String clientSecret,
		String spreadsheet,
		String oauthSettingsLocation) {
}

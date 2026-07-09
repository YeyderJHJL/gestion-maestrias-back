package com.claudecoders.masters.shared.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Hidden
public class ScalarDocsController {

	private static final String SCALAR_HTML = """
			<!doctype html>
			<html lang="es">
			<head>
				<title>UNSA Masters Management API</title>
				<meta charset="utf-8">
				<meta name="viewport" content="width=device-width, initial-scale=1">
				<link rel="icon" href="data:,">
			</head>
			<body>
				<div id="app"></div>
				<script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
				<script>
					Scalar.createApiReference('#app', {
						url: '/api/docs/openapi.json',
						pageTitle: 'UNSA Masters Management API',
						persistAuth: true
					})
				</script>
			</body>
			</html>
			""";

	@GetMapping("/api/scalar")
	@ResponseBody
	public ResponseEntity<String> scalar() {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.contentType(MediaType.TEXT_HTML)
				.body(SCALAR_HTML);
	}
}

package com.claudecoders.masters.shared.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
		info = @Info(
				title = "UNSA Masters Management API",
				version = "v1.0.0",
				description = "Postgraduate program management"
		),
		servers = @Server(url = "/", description = "Current server")
)
public class OpenApiConfig {
}

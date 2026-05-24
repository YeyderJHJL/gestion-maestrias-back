package com.claudecoders.masters.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
		info = @Info(
				title = "Masters Management API",
				version = "v1",
				description = "Academic masters management API"
		),
		servers = @Server(url = "/", description = "Current server")
)
public class OpenApiConfig {
}

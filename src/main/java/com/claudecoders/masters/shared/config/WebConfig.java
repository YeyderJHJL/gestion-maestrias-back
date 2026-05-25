package com.claudecoders.masters.shared.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.cors.allowed-origins:http://localhost:3000}")
	private String allowedOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/v1/**")
				.allowedOriginPatterns(allowedOrigins.split(","))
				.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type", "Accept")
				.maxAge(3600);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer
				.ignoreAcceptHeader(true)
				.defaultContentType(MediaType.APPLICATION_JSON);
	}

	@Bean
	static BeanPostProcessor removeXmlConverters() {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				if (bean instanceof RequestMappingHandlerAdapter adapter) {
					adapter.setMessageConverters(withoutXml(adapter.getMessageConverters()));
				} else if (bean instanceof ExceptionHandlerExceptionResolver resolver) {
					resolver.setMessageConverters(withoutXml(resolver.getMessageConverters()));
				}
				return bean;
			}

			private List<HttpMessageConverter<?>> withoutXml(List<HttpMessageConverter<?>> converters) {
				var filtered = new ArrayList<>(converters);
				filtered.removeIf(c -> c.getSupportedMediaTypes().stream()
						.anyMatch(mt -> mt.getSubtype().equals("xml") || mt.getSubtype().endsWith("+xml")));
				return filtered;
			}
		};
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(
				"/api/v1",
				HandlerTypePredicate.forBasePackage("com.claudecoders.masters")
						.and(HandlerTypePredicate.forAnnotation(RestController.class))
		);
	}
}

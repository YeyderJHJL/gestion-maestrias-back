package com.claudecoders.masters.shared.config;

import com.google.auth.Credentials;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class GcsDevConfig {

	private static final Logger log = LoggerFactory.getLogger(GcsDevConfig.class);

	@Bean
	@ConditionalOnMissingBean(Storage.class)
	Storage storage(
			@Value("${spring.cloud.gcp.project-id:masters-dev}") String projectId,
			@Value("${STORAGE_EMULATOR_HOST:http://gcs:4443}") String emulatorHost,
			@Value("${app.gcs.bucket:masters-dev}") String bucketName
	) {
		Storage storage = StorageOptions.newBuilder()
				.setProjectId(projectId)
				.setHost(emulatorHost)
				.setCredentials(noCredentials())
				.build()
				.getService();

		try {
			storage.create(BucketInfo.of(bucketName));
			log.info("GCS dev bucket '{}' created in emulator", bucketName);
		} catch (StorageException e) {
			log.debug("GCS dev bucket '{}' already exists", bucketName);
		}

		return storage;
	}

	private Credentials noCredentials() {
		return new Credentials() {
			@Override
			public String getAuthenticationType() {
				return "none";
			}

			@Override
			public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
				return Map.of();
			}

			@Override
			public boolean hasRequestMetadata() {
				return false;
			}

			@Override
			public boolean hasRequestMetadataOnly() {
				return false;
			}

			@Override
			public void refresh() throws IOException {
			}
		};
	}
}

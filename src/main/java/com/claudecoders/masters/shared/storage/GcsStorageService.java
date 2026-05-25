package com.claudecoders.masters.shared.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GcsStorageService {

	private final Storage storage;

	@Value("${app.gcs.bucket}")
	private String bucket;

	// Set in Dockerfile.dev; absent in prod — used to detect emulator mode
	@Value("${STORAGE_EMULATOR_HOST:}")
	private String emulatorHost;

	public GcsStorageService(Storage storage) {
		this.storage = storage;
	}

	public void upload(String objectKey, InputStream content, String contentType, long sizeBytes) throws IOException {
		BlobId blobId = BlobId.of(bucket, objectKey);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
				.setContentType(contentType)
				.build();
		storage.createFrom(blobInfo, content);
	}

	/**
	 * In dev (emulator), returns a direct download URL accessible from the browser via localhost.
	 * In prod, generates a signed URL valid for 15 minutes.
	 */
	public String signedDownloadUrl(String objectKey) {
		if (!emulatorHost.isBlank()) {
			String browserHost = emulatorHost.replace("//gcs:", "//localhost:");
			String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
			return "%s/download/storage/v1/b/%s/o/%s?alt=media"
					.formatted(browserHost, bucket, encodedKey);
		}
		BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, objectKey)).build();
		URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, SignUrlOption.withV4Signature());
		return url.toString();
	}

	public void delete(String objectKey) {
		storage.delete(BlobId.of(bucket, objectKey));
	}
}

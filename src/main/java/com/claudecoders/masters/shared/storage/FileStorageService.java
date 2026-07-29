package com.claudecoders.masters.shared.storage;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {
    void upload(String objectKey, InputStream content, String contentType, long sizeBytes) throws IOException;
    String signedDownloadUrl(String objectKey);
    void delete(String objectKey) throws IOException;
}

package com.claudecoders.masters.shared.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            @Value("${cloudinary.folder:masters}") String folder
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        this.folder = folder;
    }

    @Override
    public void upload(String objectKey, InputStream content, String contentType, long sizeBytes) throws IOException {
        byte[] bytes = content.readAllBytes();
        String publicId = folder + "/" + objectKey.replace('/', '_');
        cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "auto",
                "overwrite", true
        ));
    }

    @Override
    public String signedDownloadUrl(String objectKey) {
        String publicId = folder + "/" + objectKey.replace('/', '_');
        return cloudinary.url().secure(true).generate(publicId);
    }

    @Override
    public void delete(String objectKey) throws IOException {
        String publicId = folder + "/" + objectKey.replace('/', '_');
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete from Cloudinary: " + e.getMessage(), e);
        }
    }
}

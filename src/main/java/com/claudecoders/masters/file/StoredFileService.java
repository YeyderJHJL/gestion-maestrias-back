package com.claudecoders.masters.file;

import com.claudecoders.masters.file.dto.StoredFileResponse;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.storage.GcsStorageService;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StoredFileService {

	private final StoredFileRepository fileRepository;
	private final UserRepository userRepository;
	private final GcsStorageService gcsStorage;

	public StoredFileService(
			StoredFileRepository fileRepository,
			UserRepository userRepository,
			GcsStorageService gcsStorage
	) {
		this.fileRepository = fileRepository;
		this.userRepository = userRepository;
		this.gcsStorage = gcsStorage;
	}

	@Transactional(readOnly = true)
	public List<StoredFileResponse> findAll() {
		return fileRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public StoredFileResponse findById(UUID id) {
		return toResponse(getOrThrow(id));
	}

	/**
	 * Uploads a file to GCS and records its metadata in the database.
	 * The object key follows the pattern: files/{year}/{uuid}.{ext}
	 * Clients never receive the object key — only signed URLs.
	 */
	@Transactional
	public StoredFileResponse upload(MultipartFile file, UUID uploaderId) throws IOException {
		User uploader = userRepository.findById(uploaderId)
				.orElseThrow(() -> new ResourceNotFoundException("User", uploaderId));

		String ext = extension(file.getOriginalFilename());
		String objectKey = "files/%d/%s%s".formatted(LocalDate.now().getYear(), UUID.randomUUID(), ext);

		gcsStorage.upload(objectKey, file.getInputStream(), file.getContentType(), file.getSize());

		StoredFile stored = new StoredFile();
		stored.setOriginalName(file.getOriginalFilename());
		stored.setContentType(file.getContentType());
		stored.setSizeBytes(file.getSize());
		stored.setObjectKey(objectKey);
		stored.setUploadedBy(uploader);

		return toResponse(fileRepository.save(stored));
	}

	@Transactional
	public void delete(UUID id) {
		StoredFile file = getOrThrow(id);
		gcsStorage.delete(file.getObjectKey());
		fileRepository.delete(file);
	}

	private StoredFile getOrThrow(UUID id) {
		return fileRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("StoredFile", id));
	}

	private StoredFileResponse toResponse(StoredFile f) {
		return new StoredFileResponse(
				f.getId(),
				f.getOriginalName(),
				f.getContentType(),
				f.getSizeBytes(),
				f.getUploadedBy().getId(),
				f.getCreatedAt(),
				gcsStorage.signedDownloadUrl(f.getObjectKey())
		);
	}

	private String extension(String filename) {
		if (filename == null || !filename.contains(".")) return "";
		return "." + filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
	}
}

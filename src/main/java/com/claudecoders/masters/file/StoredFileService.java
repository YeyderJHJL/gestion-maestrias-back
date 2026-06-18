package com.claudecoders.masters.file;

import com.claudecoders.masters.file.dto.StoredFileResponse;
import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.storage.GcsStorageService;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StoredFileService {

	private static final String PDF = "application/pdf";
	private static final String JPEG = "image/jpeg";
	private static final String PNG = "image/png";

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

	@Transactional(readOnly = true)
	public StoredFile getReference(UUID id) {
		return getOrThrow(id);
	}

	@Transactional(readOnly = true)
	public StoredFile getReference(UUID id, FilePurpose expectedPurpose) {
		StoredFile file = getOrThrow(id);
		if (file.getPurpose() != expectedPurpose) {
			throw new BusinessException("El archivo no corresponde al tipo esperado: " + expectedPurpose.getLabel());
		}
		return file;
	}

	public StoredFileSummaryResponse toSummary(StoredFile file) {
		if (file == null) {
			return null;
		}
		return new StoredFileSummaryResponse(
				file.getId(),
				file.getOriginalName(),
				file.getContentType(),
				file.getPurpose(),
				file.getSizeBytes(),
				file.getCreatedAt()
		);
	}

	@Transactional
	public StoredFileResponse upload(MultipartFile file, UUID uploaderId, FilePurpose purpose) throws IOException {
		validateUploadRequest(file, purpose);

		User uploader = userRepository.findById(uploaderId)
				.orElseThrow(() -> new ResourceNotFoundException("User", uploaderId));

		String contentType = detectContentType(file);
		validateContentType(purpose, contentType);

		String objectKey = "files/%d/%s%s".formatted(
				LocalDate.now().getYear(),
				UUID.randomUUID(),
				extension(contentType)
		);

		gcsStorage.upload(objectKey, file.getInputStream(), contentType, file.getSize());

		StoredFile stored = new StoredFile();
		stored.setOriginalName(file.getOriginalFilename());
		stored.setContentType(contentType);
		stored.setPurpose(purpose);
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
				f.getPurpose(),
				f.getSizeBytes(),
				f.getUploadedBy().getId(),
				f.getCreatedAt(),
				gcsStorage.signedDownloadUrl(f.getObjectKey())
		);
	}

	private void validateUploadRequest(MultipartFile file, FilePurpose purpose) {
		if (purpose == null) {
			throw new BusinessException("Debe indicar el tipo de archivo");
		}
		if (file == null || file.isEmpty()) {
			throw new BusinessException("Debe enviar un archivo con contenido");
		}
		if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
			throw new BusinessException("El archivo debe tener un nombre");
		}
	}

	private void validateContentType(FilePurpose purpose, String contentType) {
		if (!allowedContentTypes(purpose).contains(contentType)) {
			throw new BusinessException("El archivo para %s debe ser %s"
					.formatted(purpose.getLabel(), allowedTypesLabel(purpose)));
		}
	}

	private Set<String> allowedContentTypes(FilePurpose purpose) {
		return switch (purpose) {
			case PAYMENT_VOUCHER -> Set.of(PDF, JPEG, PNG);
			case SYLLABUS, ENROLLMENT_RESOLUTION, REACTUALIZATION -> Set.of(PDF);
		};
	}

	private String allowedTypesLabel(FilePurpose purpose) {
		return purpose == FilePurpose.PAYMENT_VOUCHER ? "PDF, JPG o PNG" : "PDF";
	}

	private String detectContentType(MultipartFile file) throws IOException {
		byte[] header;
		try (InputStream input = file.getInputStream()) {
			header = input.readNBytes(8);
		}
		if (isPdf(header)) {
			return PDF;
		}
		if (isJpeg(header)) {
			return JPEG;
		}
		if (isPng(header)) {
			return PNG;
		}
		throw new BusinessException("Tipo de archivo no permitido. Solo se permiten PDF, JPG o PNG");
	}

	private boolean isPdf(byte[] header) {
		return header.length >= 5
				&& header[0] == '%'
				&& header[1] == 'P'
				&& header[2] == 'D'
				&& header[3] == 'F'
				&& header[4] == '-';
	}

	private boolean isJpeg(byte[] header) {
		return header.length >= 3
				&& Byte.toUnsignedInt(header[0]) == 0xFF
				&& Byte.toUnsignedInt(header[1]) == 0xD8
				&& Byte.toUnsignedInt(header[2]) == 0xFF;
	}

	private boolean isPng(byte[] header) {
		return header.length >= 8
				&& Byte.toUnsignedInt(header[0]) == 0x89
				&& header[1] == 'P'
				&& header[2] == 'N'
				&& header[3] == 'G'
				&& Byte.toUnsignedInt(header[4]) == 0x0D
				&& Byte.toUnsignedInt(header[5]) == 0x0A
				&& Byte.toUnsignedInt(header[6]) == 0x1A
				&& Byte.toUnsignedInt(header[7]) == 0x0A;
	}

	private String extension(String contentType) {
		return switch (contentType) {
			case PDF -> ".pdf";
			case JPEG -> ".jpg";
			case PNG -> ".png";
			default -> "";
		};
	}
}

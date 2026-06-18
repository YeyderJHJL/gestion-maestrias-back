package com.claudecoders.masters.file;

import com.claudecoders.masters.file.dto.StoredFileResponse;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.exception.BusinessException;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.security.SecurityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "File upload, retrieval and deletion via GCS")
public class StoredFileController {

	private final StoredFileService fileService;

	public StoredFileController(StoredFileService fileService) {
		this.fileService = fileService;
	}

	@GetMapping
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT},
		description = "List all stored files (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@Operation(summary = "List all stored files")
	public ApiResponse<List<StoredFileResponse>> findAll() {
		return ApiResponse.ok(fileService.findAll());
	}

	@GetMapping("/{id}")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT},
		description = "Get file metadata and signed download URL (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@Operation(summary = "Get file metadata and signed download URL")
	public ApiResponse<StoredFileResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(fileService.findById(id));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Authorize(roles = {UserRole.ADMIN},
		description = "Upload a file with explicit purpose (only ADMIN can access)")
	@Operation(summary = "Upload a file with explicit purpose")
	public ResponseEntity<ApiResponse<StoredFileResponse>> upload(
			@RequestParam MultipartFile file,
			@RequestParam String purpose
	) throws IOException {
		return upload(file, parsePurpose(purpose), "Archivo subido correctamente");
	}

	@PostMapping(path = "/syllabus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER},
		description = "Upload a syllabus PDF file (only ADMIN and TEACHER can access)")
	@Operation(summary = "Upload syllabus PDF")
	public ResponseEntity<ApiResponse<StoredFileResponse>> uploadSyllabus(@RequestParam MultipartFile file)
			throws IOException {
		return upload(file, FilePurpose.SYLLABUS, "Silabo subido correctamente");
	}

	@PostMapping(path = "/vouchers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Authorize(roles = {UserRole.ADMIN, UserRole.STUDENT},
		description = "Upload a payment voucher file as PDF, JPG or PNG (only ADMIN and STUDENT can access)")
	@Operation(summary = "Upload payment voucher file")
	public ResponseEntity<ApiResponse<StoredFileResponse>> uploadVoucher(@RequestParam MultipartFile file)
			throws IOException {
		return upload(file, FilePurpose.PAYMENT_VOUCHER, "Voucher subido correctamente");
	}

	@PostMapping(path = "/resolutions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Authorize(roles = {UserRole.ADMIN, UserRole.STUDENT},
		description = "Upload an enrollment resolution PDF file (only ADMIN and STUDENT can access)")
	@Operation(summary = "Upload enrollment resolution PDF")
	public ResponseEntity<ApiResponse<StoredFileResponse>> uploadResolution(@RequestParam MultipartFile file)
			throws IOException {
		return upload(file, FilePurpose.ENROLLMENT_RESOLUTION, "Resolucion subida correctamente");
	}

	@PostMapping(path = "/reactualizations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Authorize(roles = {UserRole.ADMIN, UserRole.STUDENT},
		description = "Upload a reactualization PDF file (only ADMIN and STUDENT can access)")
	@Operation(summary = "Upload reactualization PDF")
	public ResponseEntity<ApiResponse<StoredFileResponse>> uploadReactualization(@RequestParam MultipartFile file)
			throws IOException {
		return upload(file, FilePurpose.REACTUALIZATION, "Reactualizacion subida correctamente");
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Authorize(roles = {UserRole.ADMIN},
		description = "Delete a stored file (only ADMIN can access)")
	@Operation(summary = "Delete a stored file")
	public void delete(@PathVariable UUID id) {
		fileService.delete(id);
	}

	private FilePurpose parsePurpose(String purpose) {
		try {
			return FilePurpose.fromValue(purpose);
		} catch (IllegalArgumentException ex) {
			throw new BusinessException("Tipo de archivo no valido");
		}
	}

	private ResponseEntity<ApiResponse<StoredFileResponse>> upload(
			MultipartFile file,
			FilePurpose purpose,
			String message
	) throws IOException {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(fileService.upload(file, SecurityHelper.currentUserId(), purpose), message));
	}
}

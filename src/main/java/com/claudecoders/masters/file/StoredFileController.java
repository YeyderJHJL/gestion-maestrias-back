package com.claudecoders.masters.file;

import com.claudecoders.masters.file.dto.StoredFileResponse;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
	@Authorize(roles = {UserRole.ADMIN}, description = "List all stored files with metadata (no signed URLs)")
	@Operation(summary = "List all stored files")
	public ApiResponse<List<StoredFileResponse>> findAll() {
		return ApiResponse.ok(fileService.findAll());
	}

	@GetMapping("/{id}")
	@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT},
			description = "Get file metadata and signed download URL")
	@Operation(summary = "Get file metadata and signed download URL")
	public ApiResponse<StoredFileResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(fileService.findById(id));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT},
			description = "Upload a file to GCS")
	@Operation(summary = "Upload a file — requires authentication")
	public ApiResponse<StoredFileResponse> upload(@RequestParam MultipartFile file) throws IOException {
		return ApiResponse.ok(
				fileService.upload(file, SecurityHelper.currentUserId()),
				"File uploaded successfully"
		);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Authorize(roles = {UserRole.ADMIN}, description = "Delete a file from GCS and the database")
	@Operation(summary = "Delete a stored file")
	public void delete(@PathVariable UUID id) {
		fileService.delete(id);
	}
}

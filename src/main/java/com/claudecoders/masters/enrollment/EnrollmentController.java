package com.claudecoders.masters.enrollment;

import com.claudecoders.masters.enrollment.dto.EnrollmentRequest;
import com.claudecoders.masters.enrollment.dto.EnrollmentResponse;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Enrollments", description = "Enrollment management")
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

	private final EnrollmentService enrollmentService;

	public EnrollmentController(EnrollmentService enrollmentService) {
		this.enrollmentService = enrollmentService;
	}

	@Operation(summary = "List enrollments")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT }, 
		description = "List all enrollments (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@GetMapping
	public ApiResponse<List<EnrollmentResponse>> findAll() {
		return ApiResponse.ok(enrollmentService.findAll());
	}

	@Operation(summary = "Get enrollment by id")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT },
		description = "Get enrollment by id (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@GetMapping("/{id}")
	public ApiResponse<EnrollmentResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(enrollmentService.findById(id));
	}

	@Operation(summary = "Create enrollment")
	@Authorize(roles = { UserRole.ADMIN, UserRole.TEACHER }, 
		description = "Create a new enrollment (only ADMIN and TEACHER can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<EnrollmentResponse>> create(@Valid @RequestBody EnrollmentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(enrollmentService.create(request), "Enrollment created"));
	}

	@Operation(summary = "Update enrollment")
	@Authorize(roles = { UserRole.ADMIN, UserRole.TEACHER }, 
		description = "Update enrollment information (only ADMIN and TEACHER can access)")
	@PutMapping("/{id}")
	public ApiResponse<EnrollmentResponse> update(
			@PathVariable UUID id,
			@Valid @RequestBody EnrollmentRequest request
	) {
		return ApiResponse.ok(enrollmentService.update(id, request), "Enrollment updated");
	}

	@Operation(summary = "Delete enrollment")
	@Authorize(roles = { UserRole.ADMIN, UserRole.TEACHER }, 
		description = "Delete an enrollment (only ADMIN and TEACHER can access)")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		enrollmentService.delete(id);
		return ApiResponse.ok(null, "Enrollment deleted");
	}
}

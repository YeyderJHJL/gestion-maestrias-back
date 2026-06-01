package com.claudecoders.masters.assignment;

import com.claudecoders.masters.assignment.dto.AssignmentRequest;
import com.claudecoders.masters.assignment.dto.AssignmentResponse;
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

@Tag(name = "Assignments", description = "Teacher assignment management")
@RestController
@RequestMapping("/assignments")
public class AssignmentController {

	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	@Operation(summary = "List assignments")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR}, 
		description = "List all assignments (only ADMIN and COORDINATOR can access)")
	@GetMapping
	public ApiResponse<List<AssignmentResponse>> findAll() {
		return ApiResponse.ok(assignmentService.findAll());
	}

	@Operation(summary = "Get assignment by course and teacher")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR },
		description = "Get assignment by course and teacher (only ADMIN and COORDINATOR can access)")
	@GetMapping("/courses/{courseId}/teachers/{teacherId}")
	public ApiResponse<AssignmentResponse> findById(
			@PathVariable UUID courseId,
			@PathVariable UUID teacherId
	) {
		return ApiResponse.ok(assignmentService.findById(courseId, teacherId));
	}

	@Operation(summary = "Create assignment")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Create a new assignment (only ADMIN can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<AssignmentResponse>> create(@Valid @RequestBody AssignmentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(assignmentService.create(request), "Assignment created"));
	}

	@Operation(summary = "Update assignment")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Update assignment information (only ADMIN can access)")
	@PutMapping("/courses/{courseId}/teachers/{teacherId}")
	public ApiResponse<AssignmentResponse> update(
			@PathVariable UUID courseId,
			@PathVariable UUID teacherId,
			@Valid @RequestBody AssignmentRequest request
	) {
		return ApiResponse.ok(assignmentService.update(courseId, teacherId, request), "Assignment updated");
	}

	@Operation(summary = "Delete assignment")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Delete an assignment (only ADMIN can access)")
	@DeleteMapping("/courses/{courseId}/teachers/{teacherId}")
	public ApiResponse<Void> delete(@PathVariable UUID courseId, @PathVariable UUID teacherId) {
		assignmentService.delete(courseId, teacherId);
		return ApiResponse.ok(null, "Assignment deleted");
	}
}

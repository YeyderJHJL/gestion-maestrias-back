package com.claudecoders.masters.assignment;

import com.claudecoders.masters.assignment.dto.AssignmentRequest;
import com.claudecoders.masters.assignment.dto.AssignmentResponse;
import com.claudecoders.masters.assignment.dto.AssignmentSyllabusRequest;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.security.SecurityHelper;
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

	@Operation(summary = "List assignments by course")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR },
		description = "List assignments for a course (only ADMIN and COORDINATOR can access)")
	@GetMapping("/courses/{courseId}")
	public ApiResponse<List<AssignmentResponse>> findByCourse(@PathVariable UUID courseId) {
		return ApiResponse.ok(assignmentService.findByCourse(courseId));
	}

	@Operation(summary = "List assignments by teacher")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR },
		description = "List assignments for a teacher (only ADMIN and COORDINATOR can access)")
	@GetMapping("/teachers/{teacherId}")
	public ApiResponse<List<AssignmentResponse>> findByTeacher(@PathVariable UUID teacherId) {
		return ApiResponse.ok(assignmentService.findByTeacher(teacherId));
	}

	@Operation(summary = "List current authenticated teacher's assignments")
	@Authorize(roles = { UserRole.TEACHER },
		description = "List the assignments (assigned courses) of the current authenticated teacher")
	@GetMapping("/me")
	public ApiResponse<List<AssignmentResponse>> findMine() {
		return ApiResponse.ok(assignmentService.findByCurrentTeacher(SecurityHelper.currentUserId()));
	}

	@Operation(summary = "Get assignment by course, teacher and semester")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR },
		description = "Get assignment by course, teacher and semester (only ADMIN and COORDINATOR can access)")
	@GetMapping("/courses/{courseId}/teachers/{teacherId}/semesters/{semesterId}")
	public ApiResponse<AssignmentResponse> findById(
			@PathVariable UUID courseId,
			@PathVariable UUID teacherId,
			@PathVariable Integer semesterId
	) {
		return ApiResponse.ok(assignmentService.findById(courseId, teacherId, semesterId));
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
	@PutMapping("/courses/{courseId}/teachers/{teacherId}/semesters/{semesterId}")
	public ApiResponse<AssignmentResponse> update(
			@PathVariable UUID courseId,
			@PathVariable UUID teacherId,
			@PathVariable Integer semesterId,
			@Valid @RequestBody AssignmentRequest request
	) {
		return ApiResponse.ok(assignmentService.update(courseId, teacherId, semesterId, request), "Assignment updated");
	}

	@Operation(summary = "Update assignment syllabus")
	@Authorize(roles = { UserRole.TEACHER },
		description = "Update syllabus file for the current teacher assignment")
	@PutMapping("/courses/{courseId}/semesters/{semesterId}/syllabus")
	public ApiResponse<AssignmentResponse> updateSyllabus(
			@PathVariable UUID courseId,
			@PathVariable Integer semesterId,
			@Valid @RequestBody AssignmentSyllabusRequest request
	) {
		return ApiResponse.ok(
				assignmentService.updateCurrentTeacherSyllabus(
						SecurityHelper.currentUserId(),
						courseId,
						semesterId,
						request.syllabusFileId()
				),
				"Silabo actualizado correctamente"
		);
	}

	@Operation(summary = "Delete assignment")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Delete an assignment (only ADMIN can access)")
	@DeleteMapping("/courses/{courseId}/teachers/{teacherId}/semesters/{semesterId}")
	public ApiResponse<Void> delete(
			@PathVariable UUID courseId,
			@PathVariable UUID teacherId,
			@PathVariable Integer semesterId
	) {
		assignmentService.delete(courseId, teacherId, semesterId);
		return ApiResponse.ok(null, "Assignment deleted");
	}
}

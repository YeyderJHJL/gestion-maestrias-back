package com.claudecoders.masters.student;

import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.student.dto.StudentBulkRequest;
import com.claudecoders.masters.student.dto.StudentImportResponse;
import com.claudecoders.masters.student.dto.StudentRequest;
import com.claudecoders.masters.student.dto.StudentResponse;
import com.claudecoders.masters.student.dto.StudentStatusRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Students", description = "Student management")
@RestController
@RequestMapping("/students")
public class StudentController {

	private final StudentService studentService;
	private final StudentImportService studentImportService;

	public StudentController(StudentService studentService, StudentImportService studentImportService) {
		this.studentService = studentService;
		this.studentImportService = studentImportService;
	}

	@Operation(summary = "Get current authenticated student profile")
	@GetMapping("/me")
	@Authorize(roles = { UserRole.STUDENT },
		description = "Get current authenticated student profile (only STUDENT can access)")
	public ApiResponse<StudentResponse> me() {
		return ApiResponse.ok(studentService.findByUserId(SecurityHelper.currentUserId()));
	}

	@Operation(summary = "List students with optional search filters")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR},
		description = "List students; filter by yearPromotion, status and free-text search on name/email/dni/cui")
	@GetMapping
	public ApiResponse<List<StudentResponse>> findAll(
			@RequestParam(required = false) Integer yearPromotion,
			@RequestParam(required = false) StudentStatus status,
			@RequestParam(required = false) String search
	) {
		if (yearPromotion == null && status == null && (search == null || search.isBlank())) {
			return ApiResponse.ok(studentService.findAll());
		}
		return ApiResponse.ok(studentService.search(yearPromotion, status, search));
	}

	@Operation(summary = "List students by promotion year")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER},
		description = "List all students that belong to a specific promotion year")
	@GetMapping("/promotions/{year}")
	public ApiResponse<List<StudentResponse>> findByPromotion(@PathVariable Integer year) {
		return ApiResponse.ok(studentService.findByPromotion(year));
	}

	@Operation(summary = "Get student by id")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER},
		description = "Get student by id (only ADMIN, COORDINATOR and TEACHER can access)")
	@GetMapping("/{id}")
	public ApiResponse<StudentResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(studentService.findById(id));
	}

	@Operation(summary = "Create student")
	@Authorize(roles = {UserRole.ADMIN},
		description = "Create new student (only ADMIN can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<StudentResponse>> create(@Valid @RequestBody StudentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(studentService.create(request), "Student created"));
	}

	@Operation(summary = "Update student")
	@Authorize(roles = {UserRole.ADMIN},
		description = "Update student (only ADMIN can access)")
	@PutMapping("/{id}")
	public ApiResponse<StudentResponse> update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
		return ApiResponse.ok(studentService.update(id, request), "Student updated");
	}

	@Operation(summary = "Change student status and/or active flag")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR},
		description = "Update academic status (REGULAR/REACTUALIZATION) and/or toggle the user account active flag")
	@PatchMapping("/{id}/status")
	public ApiResponse<StudentResponse> changeStatus(
			@PathVariable UUID id,
			@Valid @RequestBody StudentStatusRequest request
	) {
		return ApiResponse.ok(
				studentService.changeStatus(id, request.status(), request.active()),
				"Student status updated"
		);
	}

	@Operation(summary = "Delete student")
	@Authorize(roles = {UserRole.ADMIN},
		description = "Delete student (only ADMIN can access)")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		studentService.delete(id);
		return ApiResponse.ok(null, "Student deleted");
	}

	@Operation(summary = "Bulk delete students")
	@Authorize(roles = {UserRole.ADMIN},
		description = "Soft-delete students and their associated user accounts (only ADMIN can access)")
	@DeleteMapping("/bulk")
	public ApiResponse<Void> deleteBulk(@Valid @Size(min = 1) @RequestBody List<@NotNull UUID> idStudents) {
		studentService.deleteBulk(idStudents);
		return ApiResponse.ok(null, "Students deleted");
	}

	@Operation(summary = "Bulk import students from JSON (RF-MA-18)")
	@Authorize(roles = {UserRole.ADMIN},
		description = "Bulk-import students from a JSON list (frontend pre-processes the Excel).")
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<StudentImportResponse>> importFromJson(
			@Valid @Size(min = 1) @RequestBody List<@NotNull @Valid StudentBulkRequest> items
	) {
		StudentImportResponse summary = studentImportService.importFromJson(items);
		String message = "Imported %d, rejected %d (total %d)".formatted(
				summary.imported(), summary.rejected(), summary.totalRows());
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(summary, message));
	}
}

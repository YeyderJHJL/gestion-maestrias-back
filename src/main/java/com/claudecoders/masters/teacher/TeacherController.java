package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.teacher.dto.TeacherBulkRequest;
import com.claudecoders.masters.teacher.dto.TeacherPatchRequest;
import com.claudecoders.masters.teacher.dto.TeacherRequest;
import com.claudecoders.masters.teacher.dto.TeacherResponse;
import com.claudecoders.masters.teacher.dto.TeacherImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

@Tag(name = "Teachers", description = "Teacher management")
@RestController
@RequestMapping("/teachers")
public class TeacherController {

	private final TeacherService teacherService;

	public TeacherController(TeacherService teacherService) {
		this.teacherService = teacherService;
	}

	@Operation(summary = "List teachers with optional filters")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR},
			description = "List teachers; supports filtering by category, type, academic degree and free-text search")
	@GetMapping
	public ApiResponse<List<TeacherResponse>> findAll(
			@RequestParam(required = false) TeacherCategory category,
			@RequestParam(required = false) TeacherType type,
			@RequestParam(required = false) AcademicDegree academicDegree,
			@RequestParam(required = false) String search
	) {
		if (category == null && type == null && academicDegree == null && (search == null || search.isBlank())) {
			return ApiResponse.ok(teacherService.findAll());
		}
		return ApiResponse.ok(teacherService.search(category, type, academicDegree, search));
	}

	@Operation(summary = "Get teacher by id")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT },
		description = "Get teacher by id (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@GetMapping("/{id}")
	public ApiResponse<TeacherResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(teacherService.findById(id));
	}

	@Operation(summary = "Create teacher")
	@Authorize(roles = {UserRole.ADMIN},
			description = "Create new teacher (only ADMIN can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<TeacherResponse>> create(@Valid @RequestBody TeacherRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(teacherService.create(request), "Teacher created"));
	}

	@Operation(summary = "Create teachers in bulk")
	@Authorize(roles = {UserRole.ADMIN},
			description = "Create users with TEACHER role and their teacher profiles in bulk (only ADMIN can access)")
	@PostMapping("/bulk")
	public ResponseEntity<ApiResponse<TeacherImportResult>> createBulk(
    @Valid @Size(min = 1) @RequestBody List<@NotNull @Valid TeacherBulkRequest> requests
	) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(teacherService.createBulk(requests), "Importación completada"));
	}

	@Operation(summary = "Update teacher (full replace)")
	@Authorize(roles = {UserRole.ADMIN},
			description = "Update teacher (only ADMIN can access)")
	@PutMapping("/{id}")
	public ApiResponse<TeacherResponse> update(@PathVariable UUID id, @Valid @RequestBody TeacherRequest request) {
		return ApiResponse.ok(teacherService.update(id, request), "Teacher updated");
	}

	@Operation(summary = "Partially update teacher")
	@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER},
			description = "Patch teacher fields; ADMIN can patch any teacher, TEACHER only their own profile via /me-style flows")
	@PatchMapping("/{id}")
	public ApiResponse<TeacherResponse> patch(@PathVariable UUID id, @Valid @RequestBody TeacherPatchRequest request) {
		return ApiResponse.ok(teacherService.patch(id, request), "Teacher updated");
	}

	@Operation(summary = "Delete teacher")
	@Authorize(roles = {UserRole.ADMIN},
			description = "Delete teacher (only ADMIN can access)")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		teacherService.delete(id);
		return ApiResponse.ok(null, "Teacher deleted");
	}
}

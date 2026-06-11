package com.claudecoders.masters.semester;

import com.claudecoders.masters.semester.dto.SemesterRequest;
import com.claudecoders.masters.semester.dto.SemesterResponse;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

@Tag(name = "Semesters", description = "Semester management")
@RestController
@RequestMapping("/semesters")
public class SemesterController {

	private final SemesterService semesterService;

	public SemesterController(SemesterService semesterService) {
		this.semesterService = semesterService;
	}

	@Operation(summary = "List semesters")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT },
			description = "List all semesters")
	@GetMapping
	public ApiResponse<List<SemesterResponse>> findAll() {
		return ApiResponse.ok(semesterService.findAll());
	}

	@Operation(summary = "Get semester by id")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT },
			description = "Get semester by id")
	@GetMapping("/{id}")
	public ApiResponse<SemesterResponse> findById(@PathVariable Integer id) {
		return ApiResponse.ok(semesterService.findById(id));
	}

	@Operation(summary = "Create semester")
	@Authorize(roles = { UserRole.ADMIN },
			description = "Create a new semester")
	@PostMapping
	public ResponseEntity<ApiResponse<SemesterResponse>> create(@Valid @RequestBody SemesterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(semesterService.create(request), "Semestre creado correctamente"));
	}

	@Operation(summary = "Update semester")
	@Authorize(roles = { UserRole.ADMIN },
			description = "Update semester information")
	@PutMapping("/{id}")
	public ApiResponse<SemesterResponse> update(
			@PathVariable Integer id,
			@Valid @RequestBody SemesterRequest request
	) {
		return ApiResponse.ok(semesterService.update(id, request), "Semestre actualizado correctamente");
	}

	@Operation(summary = "Delete semester")
	@Authorize(roles = { UserRole.ADMIN },
			description = "Delete a semester")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Integer id) {
		semesterService.delete(id);
		return ApiResponse.ok(null, "Semestre eliminado correctamente");
	}
}

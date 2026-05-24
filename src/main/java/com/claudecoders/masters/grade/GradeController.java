package com.claudecoders.masters.grade;

import com.claudecoders.masters.grade.dto.GradeRequest;
import com.claudecoders.masters.grade.dto.GradeResponse;
import com.claudecoders.masters.shared.exception.ApiResponse;
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

@Tag(name = "Grades", description = "Grade management")
@RestController
@RequestMapping("/grades")
public class GradeController {

	private final GradeService gradeService;

	public GradeController(GradeService gradeService) {
		this.gradeService = gradeService;
	}

	@Operation(summary = "List grades")
	@GetMapping
	public ApiResponse<List<GradeResponse>> findAll() {
		return ApiResponse.ok(gradeService.findAll());
	}

	@Operation(summary = "Get grade by id")
	@GetMapping("/{id}")
	public ApiResponse<GradeResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(gradeService.findById(id));
	}

	@Operation(summary = "Create grade")
	@PostMapping
	public ResponseEntity<ApiResponse<GradeResponse>> create(@Valid @RequestBody GradeRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(gradeService.create(request), "Grade created"));
	}

	@Operation(summary = "Update grade")
	@PutMapping("/{id}")
	public ApiResponse<GradeResponse> update(@PathVariable UUID id, @Valid @RequestBody GradeRequest request) {
		return ApiResponse.ok(gradeService.update(id, request), "Grade updated");
	}

	@Operation(summary = "Delete grade")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		gradeService.delete(id);
		return ApiResponse.ok(null, "Grade deleted");
	}
}

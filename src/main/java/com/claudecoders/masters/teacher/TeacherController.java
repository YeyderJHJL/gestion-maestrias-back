package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.teacher.dto.TeacherRequest;
import com.claudecoders.masters.teacher.dto.TeacherResponse;
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

@Tag(name = "Teachers", description = "Teacher management")
@RestController
@RequestMapping("/teachers")
public class TeacherController {

	private final TeacherService teacherService;

	public TeacherController(TeacherService teacherService) {
		this.teacherService = teacherService;
	}

	@Operation(summary = "List teachers")
	@GetMapping
	public ApiResponse<List<TeacherResponse>> findAll() {
		return ApiResponse.ok(teacherService.findAll());
	}

	@Operation(summary = "Get teacher by id")
	@GetMapping("/{id}")
	public ApiResponse<TeacherResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(teacherService.findById(id));
	}

	@Operation(summary = "Create teacher")
	@PostMapping
	public ResponseEntity<ApiResponse<TeacherResponse>> create(@Valid @RequestBody TeacherRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(teacherService.create(request), "Teacher created"));
	}

	@Operation(summary = "Update teacher")
	@PutMapping("/{id}")
	public ApiResponse<TeacherResponse> update(@PathVariable UUID id, @Valid @RequestBody TeacherRequest request) {
		return ApiResponse.ok(teacherService.update(id, request), "Teacher updated");
	}

	@Operation(summary = "Delete teacher")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		teacherService.delete(id);
		return ApiResponse.ok(null, "Teacher deleted");
	}
}

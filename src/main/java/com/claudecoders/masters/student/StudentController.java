package com.claudecoders.masters.student;

import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.student.dto.StudentRequest;
import com.claudecoders.masters.student.dto.StudentResponse;
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

@Tag(name = "Students", description = "Student management")
@RestController
@RequestMapping("/students")
public class StudentController {

	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@Operation(summary = "List students")
	@GetMapping
	public ApiResponse<List<StudentResponse>> findAll() {
		return ApiResponse.ok(studentService.findAll());
	}

	@Operation(summary = "Get student by id")
	@GetMapping("/{id}")
	public ApiResponse<StudentResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(studentService.findById(id));
	}

	@Operation(summary = "Create student")
	@PostMapping
	public ResponseEntity<ApiResponse<StudentResponse>> create(@Valid @RequestBody StudentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(studentService.create(request), "Student created"));
	}

	@Operation(summary = "Update student")
	@PutMapping("/{id}")
	public ApiResponse<StudentResponse> update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
		return ApiResponse.ok(studentService.update(id, request), "Student updated");
	}

	@Operation(summary = "Delete student")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		studentService.delete(id);
		return ApiResponse.ok(null, "Student deleted");
	}
}

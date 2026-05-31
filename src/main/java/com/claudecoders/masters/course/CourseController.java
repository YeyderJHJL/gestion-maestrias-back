package com.claudecoders.masters.course;

import com.claudecoders.masters.course.dto.CourseRequest;
import com.claudecoders.masters.course.dto.CourseResponse;
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

@Tag(name = "Courses", description = "Course management")
@RestController
@RequestMapping("/courses")
public class CourseController {

	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@Operation(summary = "List courses")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT }, 
		description = "List all courses (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@GetMapping
	public ApiResponse<List<CourseResponse>> findAll() {
		return ApiResponse.ok(courseService.findAll());
	}

	@Operation(summary = "Get course by id")
	@Authorize(roles = { UserRole.ADMIN, UserRole.COORDINATOR, UserRole.TEACHER, UserRole.STUDENT },
		description = "Get course by id (only ADMIN, COORDINATOR, TEACHER and STUDENT can access)")
	@GetMapping("/{id}")
	public ApiResponse<CourseResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(courseService.findById(id));
	}

	@Operation(summary = "Create course")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Create a new course (only ADMIN can access)")
	@PostMapping
	public ResponseEntity<ApiResponse<CourseResponse>> create(@Valid @RequestBody CourseRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(courseService.create(request), "Course created"));
	}

	@Operation(summary = "Update course")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Update course information (only ADMIN can access)")
	@PutMapping("/{id}")
	public ApiResponse<CourseResponse> update(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
		return ApiResponse.ok(courseService.update(id, request), "Course updated");
	}

	@Operation(summary = "Delete course")
	@Authorize(roles = { UserRole.ADMIN }, 
		description = "Delete a course (only ADMIN can access)")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		courseService.delete(id);
		return ApiResponse.ok(null, "Course deleted");
	}
}

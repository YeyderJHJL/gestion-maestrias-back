package com.claudecoders.masters.user;

import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.user.dto.UserRequest;
import com.claudecoders.masters.user.dto.UserResponse;
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

@Tag(name = "Users", description = "User management")
@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT},
			description = "Get current authenticated user profile")
	@Operation(summary = "Get current authenticated user profile")
	public ApiResponse<UserResponse> me() {
		return ApiResponse.ok(userService.findById(SecurityHelper.currentUserId()));
	}

	@Operation(summary = "List users")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users listed")
	@GetMapping
	public ApiResponse<List<UserResponse>> findAll() {
		return ApiResponse.ok(userService.findAll());
	}

	@Operation(summary = "Get user by id")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
	@GetMapping("/{id}")
	public ApiResponse<UserResponse> findById(@PathVariable UUID id) {
		return ApiResponse.ok(userService.findById(id));
	}

	@Operation(summary = "Create user")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
	@PostMapping
	public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(userService.create(request), "User created"));
	}

	@Operation(summary = "Update user")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
	@PutMapping("/{id}")
	public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
		return ApiResponse.ok(userService.update(id, request), "User updated");
	}

	@Operation(summary = "Delete user")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		userService.delete(id);
		return ApiResponse.ok(null, "User deleted");
	}
}

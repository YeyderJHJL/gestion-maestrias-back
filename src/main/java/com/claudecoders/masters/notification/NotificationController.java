package com.claudecoders.masters.notification;

import com.claudecoders.masters.notification.dto.NotificationRequest;
import com.claudecoders.masters.notification.dto.NotificationResponse;
import com.claudecoders.masters.notification.dto.UnreadCountResponse;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.dto.PageResponse;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications", description = "Notification management")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService service;

	public NotificationController(NotificationService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Get my notifications")
	@Authorize(roles = {
			UserRole.ADMIN,
			UserRole.COORDINATOR,
			UserRole.TEACHER,
			UserRole.STUDENT
	})
	public ApiResponse<PageResponse<NotificationResponse>> myNotifications(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		return ApiResponse.ok(
				service.myNotifications(page, size)
		);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get notification by id")
	public ApiResponse<NotificationResponse> findById(
			@PathVariable Long id
	) {
		return ApiResponse.ok(service.findById(id));
	}

	@PostMapping
	@Operation(summary = "Create notification")
	public ApiResponse<NotificationResponse> create(
			@Valid @RequestBody NotificationRequest request
	) {
		return ApiResponse.ok(service.create(request));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update notification")
	public ApiResponse<NotificationResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody NotificationRequest request
	) {
		return ApiResponse.ok(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete notification")
	public ApiResponse<Void> delete(
			@PathVariable Long id
	) {
		service.delete(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/unread-count")
	@Operation(summary = "Get unread notifications count")
	public ApiResponse<UnreadCountResponse> unreadCount() {
		return ApiResponse.ok(
				new UnreadCountResponse(
						service.unreadCount()
				)
		);
	}

	@PatchMapping("/{id}/read")
	@Operation(summary = "Mark notification as read")
	public ApiResponse<Void> markAsRead(
			@PathVariable Long id
	) {
		service.markAsRead(id);
		return ApiResponse.ok(null);
	}

	@PatchMapping("/read-all")
	@Operation(summary = "Mark all notifications as read")
	public ApiResponse<Void> markAllAsRead() {
		service.markAllAsRead();
		return ApiResponse.ok(null);
	}
}
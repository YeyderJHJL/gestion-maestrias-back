package com.claudecoders.masters.notification;

import com.claudecoders.masters.notification.dto.NotificationRequest;
import com.claudecoders.masters.notification.dto.NotificationResponse;
import com.claudecoders.masters.shared.dto.PageMapper;
import com.claudecoders.masters.shared.dto.PageResponse;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.user.User;
import com.claudecoders.masters.user.UserService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

	private final NotificationRepository repository;
	private final UserService userService;

	public NotificationService(
			NotificationRepository repository,
			UserService userService
	) {
		this.repository = repository;
		this.userService = userService;
	}

	public NotificationResponse create(NotificationRequest request) {

		User user = userService.getReference(request.userId());

		Notification notification = new Notification();
		notification.setUser(user);
		notification.setType(request.type());
		notification.setMessage(request.message());
		notification.setEntityType(request.entityType());
		notification.setEntityId(request.entityId());

		return toResponse(repository.save(notification));
	}

	public NotificationResponse update(
			Long id,
			NotificationRequest request
	) {

		Notification notification = repository.findById(id)
				.orElseThrow();

		User user = userService.getReference(request.userId());

		notification.setUser(user);
		notification.setType(request.type());
		notification.setMessage(request.message());
		notification.setEntityType(request.entityType());
		notification.setEntityId(request.entityId());

		return toResponse(repository.save(notification));
	}

	public NotificationResponse findById(Long id) {
		return toResponse(
				repository.findById(id)
						.orElseThrow()
		);
	}

	public void delete(Long id) {
		repository.deleteById(id);
	}

  public PageResponse<NotificationResponse> myNotifications(
      Integer page,
      Integer size
  ) {

	UUID userId = SecurityHelper.currentPrincipal().userId();

	PageRequest pageable = PageRequest.of(
			page == null ? 0 : page,
			size == null ? 20 : size
	);

	Page<NotificationResponse> notifications =
			repository
					.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
					.map(this::toResponse);

	return PageMapper.from(notifications);
}

	public long unreadCount() {

		UUID userId = SecurityHelper.currentPrincipal().userId();

		return repository.countByUser_IdAndReadAtIsNull(userId);
	}

	public void markAsRead(Long id) {

		UUID userId = SecurityHelper.currentPrincipal().userId();

		Notification notification = repository
				.findByIdAndUser_Id(id, userId)
				.orElseThrow();

		if (notification.getReadAt() == null) {
			notification.setReadAt(Instant.now());
			repository.save(notification);
		}
	}

	public void markAllAsRead() {

		UUID userId = SecurityHelper.currentPrincipal().userId();

		repository
				.findByUser_IdOrderByCreatedAtDesc(
						userId,
						PageRequest.of(0, Integer.MAX_VALUE)
				)
				.forEach(notification -> {
					if (notification.getReadAt() == null) {
						notification.setReadAt(Instant.now());
					}
				});
	}

	public Notification createInternal(
			UUID userId,
			NotificationType type,
			String message,
			String entityType,
			UUID entityId
	) {

		User user = userService.getReference(userId);

		Notification notification = new Notification();
		notification.setUser(user);
		notification.setType(type);
		notification.setMessage(message);
		notification.setEntityType(entityType);
		notification.setEntityId(entityId);

		return repository.save(notification);
	}

	private NotificationResponse toResponse(Notification notification) {

	return new NotificationResponse(
			notification.getId(),
			notification.getUser().getId(),
			notification.getType().name(),
			notification.getType().getLabel(),
			notification.getMessage(),
			notification.getEntityType(),
			notification.getEntityId(),
      notification.getReadAt() != null,
			notification.getReadAt(),
			notification.getCreatedAt()
	);
  }
}
package com.claudecoders.masters.notification;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByUser_IdOrderByCreatedAtDesc(
			UUID userId,
			Pageable pageable
	);

	Optional<Notification> findByIdAndUser_Id(
			Long id,
			UUID userId
	);

	long countByUser_IdAndReadAtIsNull(
			UUID userId
	);

	void deleteByIdAndUserId(
			Long id,
			UUID userId
	);

	long countByUser_Id(
			UUID userId
	);

	int deleteByUser_IdAndReadAtIsNotNull(
			UUID userId
	);
}
package com.claudecoders.masters.teacher;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

	Optional<Teacher> findByUser_Id(UUID userId);
}

package com.claudecoders.masters.enrollment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
  List<Enrollment> findByCourse_IdOrderByEnrollmentDateAsc(UUID courseId);

  List<Enrollment> findByStudent_IdOrderByEnrollmentDateAsc(UUID studentId);

  List<Enrollment> findByStudent_YearPromotionOrderByEnrollmentDateAsc(Integer yearPromotion);

  boolean existsByStudent_IdAndCourse_Id(UUID studentId, UUID courseId);

  boolean existsByStudent_User_IdAndCourse_IdAndState_Code(UUID userId, UUID courseId, String stateCode);

  Optional<Enrollment> findByCourse_IdAndStudent_Cui(UUID courseId, String cui);
}
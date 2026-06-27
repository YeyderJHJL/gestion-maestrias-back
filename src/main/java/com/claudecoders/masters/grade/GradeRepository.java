package com.claudecoders.masters.grade;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GradeRepository extends JpaRepository<Grade, UUID> {
  @Query("""
    SELECT g FROM Grade g
    JOIN FETCH g.enrollment e
    JOIN FETCH e.course c
    JOIN FETCH e.student s
    WHERE (:enrollmentId IS NULL OR e.id = :enrollmentId)
    AND (:courseId IS NULL OR c.id = :courseId)
    AND (:studentId IS NULL OR s.id = :studentId)
  """)
  List<Grade> findAllWithFilters(
    @Param("enrollmentId") UUID enrollmentId,
    @Param("courseId") UUID courseId,
    @Param("studentId") UUID studentId
  );
}

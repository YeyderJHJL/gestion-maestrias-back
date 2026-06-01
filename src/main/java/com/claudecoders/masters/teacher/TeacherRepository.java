package com.claudecoders.masters.teacher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

	Optional<Teacher> findByUser_Id(UUID userId);

	@Query("""
			SELECT t FROM Teacher t
			JOIN FETCH t.user u
			WHERE (:category IS NULL OR t.category = :category)
			  AND (:type IS NULL OR t.type = :type)
			  AND (:academicDegree IS NULL OR t.academicDegree = :academicDegree)
			  AND (
			    :search IS NULL
			    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			""")
	List<Teacher> search(
			@Param("category") TeacherCategory category,
			@Param("type") TeacherType type,
			@Param("academicDegree") AcademicDegree academicDegree,
			@Param("search") String search
	);
}

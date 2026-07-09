package com.claudecoders.masters.student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, UUID> {

	Optional<Student> findByUser_Id(UUID userId);

	boolean existsByCui(String cui);

	boolean existsByPaymentCode(String paymentCode);

	List<Student> findByYearPromotionOrderByUser_LastNameAscUser_FirstNameAsc(Integer yearPromotion);

	@Query("""
			SELECT s FROM Student s
			JOIN FETCH s.user u
			WHERE (:yearPromotion IS NULL OR s.yearPromotion = :yearPromotion)
			  AND (:status IS NULL OR s.status = :status)
			  AND (
			    :search IS NULL
			    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
			    OR u.dni LIKE CONCAT('%', :search, '%')
			    OR LOWER(s.cui) LIKE LOWER(CONCAT('%', :search, '%'))
			  )
			ORDER BY u.lastName ASC, u.firstName ASC
			""")
	List<Student> search(
			@Param("yearPromotion") Integer yearPromotion,
			@Param("status") StudentStatus status,
			@Param("search") String search
	);
}

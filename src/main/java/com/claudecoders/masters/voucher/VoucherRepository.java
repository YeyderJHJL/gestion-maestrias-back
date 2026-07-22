package com.claudecoders.masters.voucher;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

	@Query("""
			SELECT DISTINCT voucher
			FROM Voucher voucher
			JOIN voucher.payments voucherPayment
			WHERE voucherPayment.payment.student.id = :studentId
			ORDER BY voucher.createdAt DESC
			""")
	List<Voucher> findByPaymentStudentIdOrderByCreatedAtDesc(@Param("studentId") UUID studentId);

	@Query("""
			SELECT DISTINCT voucher
			FROM Voucher voucher
			JOIN voucher.payments voucherPayment
			JOIN voucherPayment.payment.student student
			JOIN student.user user
			WHERE (:search IS NULL
				OR LOWER(student.cui) LIKE LOWER(CONCAT('%', :search, '%'))
				OR LOWER(user.dni) LIKE LOWER(CONCAT('%', :search, '%')))
			ORDER BY voucher.createdAt DESC
			""")
	List<Voucher> search(@Param("search") String search);
}

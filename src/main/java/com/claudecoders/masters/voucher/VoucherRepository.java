package com.claudecoders.masters.voucher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

	Optional<Voucher> findFirstByPayment_IdOrderByCreatedAtDesc(UUID paymentId);

	List<Voucher> findByPayment_Student_IdOrderByCreatedAtDesc(UUID studentId);
}

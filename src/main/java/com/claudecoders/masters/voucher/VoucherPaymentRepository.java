package com.claudecoders.masters.voucher;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherPaymentRepository extends JpaRepository<VoucherPayment, VoucherPaymentId> {

	Optional<VoucherPayment> findFirstByPayment_IdOrderByVoucher_CreatedAtDesc(UUID paymentId);
}

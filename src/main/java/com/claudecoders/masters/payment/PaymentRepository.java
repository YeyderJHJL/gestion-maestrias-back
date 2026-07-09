package com.claudecoders.masters.payment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	List<Payment> findByStudent_IdOrderByPaymentNumberAsc(UUID studentId);
}

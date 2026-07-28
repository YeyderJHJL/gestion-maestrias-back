package com.claudecoders.masters.payment;

import com.claudecoders.masters.payment.dto.PaymentResponse;
import com.claudecoders.masters.shared.exception.ResourceNotFoundException;
import com.claudecoders.masters.shared.security.SecurityHelper;
import com.claudecoders.masters.student.Student;
import com.claudecoders.masters.student.StudentService;
import com.claudecoders.masters.voucher.VoucherPayment;
import com.claudecoders.masters.voucher.VoucherPaymentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final VoucherPaymentRepository voucherPaymentRepository;
	private final StudentService studentService;

	public PaymentService(
			PaymentRepository paymentRepository,
			VoucherPaymentRepository voucherPaymentRepository,
			StudentService studentService
	) {
		this.paymentRepository = paymentRepository;
		this.voucherPaymentRepository = voucherPaymentRepository;
		this.studentService = studentService;
	}

	@Transactional(readOnly = true)
	public Payment getReference(UUID id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Payment", id));
	}

	@Transactional(readOnly = true)
	public List<PaymentResponse> findMyPayments() {
		Student student = studentService.getReferenceByUserId(SecurityHelper.currentUserId());
		return paymentRepository.findByStudent_IdOrderByPaymentNumberAsc(student.getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<Payment> findByStudentId(UUID studentId) {
		return paymentRepository.findByStudent_IdOrderByPaymentNumberAsc(studentId);
	}

	@Transactional(readOnly = true)
	public List<PaymentResponse> findPaymentResponsesByStudentId(UUID studentId) {
		return findByStudentId(studentId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public String latestVoucherStateCode(UUID paymentId) {
		return voucherPaymentRepository
				.findFirstByPayment_IdOrderByVoucher_CreatedAtDesc(paymentId)
				.map(VoucherPayment::getVoucher)
				.map(voucher -> voucher.getState())
				.map(state -> state.getCode())
				.orElse(null);
	}

	private PaymentResponse toResponse(Payment payment) {
		String latestVoucherStateCode = latestVoucherStateCode(payment.getId());
		return new PaymentResponse(
				payment.getId(),
				payment.getPaymentNumber(),
				payment.getConcept(),
				payment.getAmount(),
				payment.getPaymentDate(),
				latestVoucherStateCode,
				payment.getCreatedAt(),
				payment.getUpdatedAt()
		);
	}
}

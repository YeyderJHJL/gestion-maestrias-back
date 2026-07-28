package com.claudecoders.masters.payment;

import com.claudecoders.masters.payment.dto.PaymentResponse;
import com.claudecoders.masters.shared.enums.UserRole;
import com.claudecoders.masters.shared.exception.ApiResponse;
import com.claudecoders.masters.shared.security.Authorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments", description = "Payment management")
@RestController
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Operation(summary = "List my payments",
			description = "Returns the authenticated student's payments so they know which one to attach a voucher to")
	@Authorize(roles = {UserRole.STUDENT},
			description = "List the authenticated student's own payments (only STUDENT can access)")
	@GetMapping("/my")
	public ApiResponse<List<PaymentResponse>> findMyPayments() {
		return ApiResponse.ok(paymentService.findMyPayments());
	}

	@Operation(summary = "List a student's payments",
			description = "Returns a student's payments with their latest voucher state, for review purposes")
	@Authorize(roles = {UserRole.ADMIN, UserRole.COORDINATOR},
			description = "List a student's payments (only ADMIN and COORDINATOR can access)")
	@GetMapping("/student/{studentId}")
	public ApiResponse<List<PaymentResponse>> findByStudentId(@PathVariable UUID studentId) {
		return ApiResponse.ok(paymentService.findPaymentResponsesByStudentId(studentId));
	}
}

package com.claudecoders.masters.voucher.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record VoucherPaymentResponse(
		UUID paymentId,
		Integer paymentNumber,
		String paymentConcept,
		BigDecimal paymentAmount,
		LocalDate paymentDate
) {
}

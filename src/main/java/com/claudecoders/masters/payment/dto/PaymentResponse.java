package com.claudecoders.masters.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Payment response")
public record PaymentResponse(
		UUID id,
		Integer paymentNumber,
		String concept,
		BigDecimal amount,
		LocalDate paymentDate,
		@Schema(description = "State code of the latest voucher for this payment, null if no voucher uploaded yet")
		String latestVoucherStateCode,
		Instant createdAt,
		Instant updatedAt
) {
}

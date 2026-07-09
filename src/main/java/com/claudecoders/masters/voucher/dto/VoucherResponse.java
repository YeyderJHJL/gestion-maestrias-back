package com.claudecoders.masters.voucher.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VoucherResponse(
		UUID id,
		UUID paymentId,
		Integer paymentNumber,
		String paymentConcept,
		BigDecimal paymentAmount,
		LocalDate paymentDate,
		String studentName,
		String studentEmail,
		String studentPaymentCode,
		Integer stateId,
		String stateCode,
		String stateName,
		StoredFileSummaryResponse file,
		String observation,
		Instant createdAt,
		Instant updatedAt
) {
}

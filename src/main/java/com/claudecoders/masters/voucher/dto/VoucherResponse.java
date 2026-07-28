package com.claudecoders.masters.voucher.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VoucherResponse(
		UUID id,
		BigDecimal declaredAmount,
		List<VoucherPaymentResponse> payments,
		UUID studentId,
		String studentName,
		String studentEmail,
		String studentPaymentCode,
		String studentDni,
		String studentCui,
		Integer stateId,
		String stateCode,
		String stateName,
		StoredFileSummaryResponse file,
		String observation,
		String operationNumber,
		Instant createdAt,
		Instant updatedAt
) {
}

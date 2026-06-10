package com.claudecoders.masters.voucher.dto;

import com.claudecoders.masters.file.dto.StoredFileSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VoucherResponse(
		UUID id,
		UUID paymentId,
		Integer stateId,
		String stateCode,
		String stateName,
		StoredFileSummaryResponse file,
		BigDecimal declaredAmount,
		String observation,
		Instant createdAt,
		Instant updatedAt
) {
}

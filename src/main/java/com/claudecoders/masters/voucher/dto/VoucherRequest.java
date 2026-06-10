package com.claudecoders.masters.voucher.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record VoucherRequest(
		@NotNull UUID paymentId,
		@NotNull Integer stateId,
		@NotNull UUID fileId,
		@NotNull BigDecimal declaredAmount,
		String observation
) {
}

package com.claudecoders.masters.voucher.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VoucherRequest(
		@NotNull UUID paymentId,
		@NotNull Integer stateId,
		@NotNull UUID fileId,
		String observation
) {
}

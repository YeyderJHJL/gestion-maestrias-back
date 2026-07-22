package com.claudecoders.masters.voucher.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record VoucherRequest(
		@NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal declaredAmount,
		@NotEmpty List<@Valid VoucherPaymentRequest> payments,
		@NotNull Integer stateId,
		@NotNull UUID fileId,
		String observation
) {
}

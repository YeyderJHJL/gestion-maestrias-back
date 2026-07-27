package com.claudecoders.masters.voucher.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VoucherPaymentRequest(@NotNull UUID paymentId) {
}

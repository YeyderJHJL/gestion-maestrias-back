package com.claudecoders.masters.voucher;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class VoucherPaymentId implements Serializable {

	@Column(name = "id_voucher")
	private UUID voucherId;

	@Column(name = "id_payment")
	private UUID paymentId;

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof VoucherPaymentId that)) {
			return false;
		}
		return Objects.equals(voucherId, that.voucherId) && Objects.equals(paymentId, that.paymentId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(voucherId, paymentId);
	}
}

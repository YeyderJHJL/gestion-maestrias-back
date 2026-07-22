package com.claudecoders.masters.voucher;

import com.claudecoders.masters.payment.Payment;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "voucher_payments")
public class VoucherPayment {

	@EmbeddedId
	private VoucherPaymentId id = new VoucherPaymentId();

	@MapsId("voucherId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_voucher", nullable = false)
	private Voucher voucher;

	@MapsId("paymentId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_payment", nullable = false)
	private Payment payment;

	public VoucherPayment() {
	}

	public VoucherPayment(Voucher voucher, Payment payment) {
		this.voucher = voucher;
		this.payment = payment;
	}

	public Voucher getVoucher() {
		return voucher;
	}

	public Payment getPayment() {
		return payment;
	}
}

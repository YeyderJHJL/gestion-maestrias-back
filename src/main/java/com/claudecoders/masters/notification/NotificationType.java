package com.claudecoders.masters.notification;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType implements LabeledEnum {
	VOUCHER_UPLOADED("Voucher cargado"),
	VOUCHER_VALIDATED("Voucher validado"),
	VOUCHER_OBSERVED("Voucher observado"),
	VOUCHER_REJECTED("Voucher rechazado"),
	GRADE_REGISTERED("Nota registrada"),
	GRADE_MODIFIED("Nota modificada"),
	ENROLLMENT_UPDATED("Matricula actualizada");

	private final String label;

	NotificationType(String label) {
		this.label = label;
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

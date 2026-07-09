package com.claudecoders.masters.file;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FilePurpose implements LabeledEnum {
	SYLLABUS("Silabo"),
	PAYMENT_VOUCHER("Voucher de pago"),
	ENROLLMENT_RESOLUTION("Resolucion de matricula"),
	REACTUALIZATION("Reactualizacion");

	private final String label;

	FilePurpose(String label) {
		this.label = label;
	}

	@JsonCreator
	public static FilePurpose fromValue(String value) {
		return LabeledEnum.fromValue(FilePurpose.class, value);
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

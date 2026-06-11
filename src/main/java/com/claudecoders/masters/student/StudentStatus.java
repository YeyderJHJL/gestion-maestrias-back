package com.claudecoders.masters.student;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StudentStatus implements LabeledEnum {
	REGULAR("Regular"),
	REACTUALIZATION("Reactualizacion");

	private final String label;

	StudentStatus(String label) {
		this.label = label;
	}

	@JsonCreator
	public static StudentStatus fromValue(String value) {
		return LabeledEnum.fromValue(StudentStatus.class, value);
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

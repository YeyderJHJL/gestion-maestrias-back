package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TeacherType implements LabeledEnum {
	INTERNAL("Interno"),
	EXTERNAL("Externo");

	private final String label;

	TeacherType(String label) {
		this.label = label;
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AcademicDegree implements LabeledEnum {
	MASTER("Magister"),
	DOCTOR("Doctor");

	private final String label;

	AcademicDegree(String label) {
		this.label = label;
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

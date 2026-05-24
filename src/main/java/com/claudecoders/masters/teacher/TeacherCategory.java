package com.claudecoders.masters.teacher;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TeacherCategory implements LabeledEnum {
	PRINCIPAL("Principal"),
	ASSOCIATE("Asociado"),
	AUXILIARY("Auxiliar");

	private final String label;

	TeacherCategory(String label) {
		this.label = label;
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

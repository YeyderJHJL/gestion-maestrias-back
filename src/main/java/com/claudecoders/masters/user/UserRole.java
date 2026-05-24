package com.claudecoders.masters.user;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole implements LabeledEnum {
	ADMIN("Administrador"),
	TEACHER("Docente"),
	STUDENT("Estudiante");

	private final String label;

	UserRole(String label) {
		this.label = label;
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

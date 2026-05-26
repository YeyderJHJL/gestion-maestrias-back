package com.claudecoders.masters.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole implements LabeledEnum {
	ADMIN("Administrador"),
	TEACHER("Docente"),
	STUDENT("Estudiante"),
	COORDINATOR("Coordinador");

	private final String label;

	UserRole(String label) {
		this.label = label;
	}

	@JsonCreator
	public static UserRole fromValue(String value) {
		return LabeledEnum.fromValue(UserRole.class, value);
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

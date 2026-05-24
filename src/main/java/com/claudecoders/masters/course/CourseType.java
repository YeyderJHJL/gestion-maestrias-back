package com.claudecoders.masters.course;

import com.claudecoders.masters.shared.enums.LabeledEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CourseType implements LabeledEnum {
	REGULAR("Regular"),
	THESIS("Tesis"),
	TOPICS("Topicos");

	private final String label;

	CourseType(String label) {
		this.label = label;
	}

	@JsonCreator
	public static CourseType fromValue(String value) {
		return LabeledEnum.fromValue(CourseType.class, value);
	}

	@Override
	@JsonValue
	public String getLabel() {
		return label;
	}
}

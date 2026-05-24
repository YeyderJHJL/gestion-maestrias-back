package com.claudecoders.masters.shared.enums;

public interface LabeledEnum {

	String getLabel();

	static <E extends Enum<E> & LabeledEnum> E fromValue(Class<E> enumType, String value) {
		for (E option : enumType.getEnumConstants()) {
			if (option.name().equalsIgnoreCase(value) || option.getLabel().equalsIgnoreCase(value)) {
				return option;
			}
		}
		throw new IllegalArgumentException("Unknown %s value: %s".formatted(enumType.getSimpleName(), value));
	}
}

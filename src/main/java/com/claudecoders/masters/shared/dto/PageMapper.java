package com.claudecoders.masters.shared.dto;

import org.springframework.data.domain.Page;

public final class PageMapper {

	private PageMapper() {
	}

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.getNumber(),
				page.getSize()
		);
	}
}
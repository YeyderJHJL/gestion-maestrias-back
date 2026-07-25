package com.claudecoders.masters.grade.dto;

import java.util.List;
import java.util.UUID;

public record GradeBulkRowResult(
        Integer rowNumber,
        String cui,
        Status status,
        UUID gradeId,
        List<String> observations
) {
    public enum Status {
        IMPORTED,
        REJECTED
    }
}
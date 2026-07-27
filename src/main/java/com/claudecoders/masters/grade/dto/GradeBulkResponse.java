package com.claudecoders.masters.grade.dto;

import java.util.List;

public record GradeBulkResponse(
        int totalRows,
        int imported,
        int rejected,
        List<GradeBulkRowResult> results
) {
}
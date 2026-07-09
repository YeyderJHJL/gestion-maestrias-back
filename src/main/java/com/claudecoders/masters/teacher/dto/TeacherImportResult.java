package com.claudecoders.masters.teacher.dto;

import java.util.List;

public record TeacherImportResult(
        int imported,
        int rejected,
        List<TeacherImportRowResult> results
) {
}
package com.claudecoders.masters.teacher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeacherImportRowResult(
        int row,
        String status,   // "SUCCESS" | "REJECTED"
        String email,
        String reason,          // solo en REJECTED
        TeacherResponse teacher // solo en SUCCESS
) {
}
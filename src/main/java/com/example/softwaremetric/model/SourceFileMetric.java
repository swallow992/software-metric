package com.example.softwaremetric.model;

public record SourceFileMetric(
        String sourceFile,
        int totalLines,
        int blankLines,
        int commentLines,
        int codeLines
) {
}

package com.example.softwaremetric.model;

import java.util.List;

public record SourceAnalysisResult(
        String sourceRoot,
        int javaFileCount,
        int packageCount,
        int classCount,
        int interfaceCount,
        int methodCount,
        int fieldCount,
        int totalLines,
        int blankLines,
        int commentLines,
        int codeLines,
        int totalCyclomaticComplexity,
        double averageMethodCyclomaticComplexity,
        List<SourceFileMetric> fileMetrics,
        List<JavaTypeInfo> types
) {

    public boolean hasTypes() {
        return !types.isEmpty();
    }

    public boolean hasFileMetrics() {
        return !fileMetrics.isEmpty();
    }
}

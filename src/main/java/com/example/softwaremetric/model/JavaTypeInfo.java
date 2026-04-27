package com.example.softwaremetric.model;

import java.util.List;

public record JavaTypeInfo(
        String sourceFile,
        String packageName,
        String name,
        String qualifiedName,
        String kind,
        String superClassName,
        List<String> interfaces,
        List<JavaMethodInfo> methods,
        List<JavaFieldInfo> fields,
        List<String> typeDependencies,
        CkMetricInfo ckMetric
) {

    public int methodCount() {
        return methods.size();
    }

    public int fieldCount() {
        return fields.size();
    }

    public int totalCyclomaticComplexity() {
        return methods.stream().mapToInt(JavaMethodInfo::cyclomaticComplexity).sum();
    }

    public double averageCyclomaticComplexity() {
        if (methods.isEmpty()) {
            return 0;
        }
        return (double) totalCyclomaticComplexity() / methods.size();
    }

    public JavaTypeInfo withCkMetric(CkMetricInfo nextCkMetric) {
        return new JavaTypeInfo(
                sourceFile,
                packageName,
                name,
                qualifiedName,
                kind,
                superClassName,
                interfaces,
                methods,
                fields,
                typeDependencies,
                nextCkMetric
        );
    }
}

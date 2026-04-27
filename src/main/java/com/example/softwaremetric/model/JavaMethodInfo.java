package com.example.softwaremetric.model;

import java.util.List;

public record JavaMethodInfo(
        String name,
        String returnType,
        List<String> parameters,
        String modifiers,
        int cyclomaticComplexity,
        List<String> invokedMethods,
        List<String> accessedFields
) {
}

package com.example.softwaremetric.metric;

import com.example.softwaremetric.model.CkMetricInfo;
import com.example.softwaremetric.model.JavaMethodInfo;
import com.example.softwaremetric.model.JavaTypeInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CkMetricCalculator {

    private static final Set<String> IGNORED_DEPENDENCIES = Set.of(
            "String", "Object", "Integer", "Long", "Double", "Float", "Boolean",
            "Character", "Byte", "Short", "Void", "System", "Math"
    );

    public List<JavaTypeInfo> calculate(List<JavaTypeInfo> types) {
        TypeIndex typeIndex = new TypeIndex(types);
        Map<String, Integer> numberOfChildrenByType = numberOfChildrenByType(types, typeIndex);
        Map<String, Integer> inheritanceDepthByType = inheritanceDepthByType(types, typeIndex);

        List<JavaTypeInfo> enrichedTypes = new ArrayList<>();
        for (JavaTypeInfo type : types) {
            CkMetricInfo metric = new CkMetricInfo(
                    wmc(type),
                    inheritanceDepthByType.getOrDefault(type.qualifiedName(), 0),
                    numberOfChildrenByType.getOrDefault(type.qualifiedName(), 0),
                    cbo(type),
                    rfc(type),
                    lcom(type),
                    riskLevel(type, inheritanceDepthByType, numberOfChildrenByType)
            );
            enrichedTypes.add(type.withCkMetric(metric));
        }
        return enrichedTypes;
    }

    private int wmc(JavaTypeInfo type) {
        return type.totalCyclomaticComplexity();
    }

    private int cbo(JavaTypeInfo type) {
        return type.typeDependencies()
                .stream()
                .map(this::simpleName)
                .filter(dependency -> !isIgnoredDependency(type, dependency))
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .size();
    }

    private int rfc(JavaTypeInfo type) {
        Set<String> responseSet = type.methods()
                .stream()
                .map(JavaMethodInfo::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        type.methods().stream()
                .flatMap(method -> method.invokedMethods().stream())
                .forEach(responseSet::add);

        return responseSet.size();
    }

    private int lcom(JavaTypeInfo type) {
        if (type.fieldCount() == 0 || type.methodCount() < 2) {
            return 0;
        }

        int noSharedFields = 0;
        int sharedFields = 0;
        List<JavaMethodInfo> methods = type.methods();

        for (int left = 0; left < methods.size(); left++) {
            Set<String> leftFields = new HashSet<>(methods.get(left).accessedFields());
            for (int right = left + 1; right < methods.size(); right++) {
                Set<String> rightFields = new HashSet<>(methods.get(right).accessedFields());
                leftFields.retainAll(rightFields);
                if (leftFields.isEmpty()) {
                    noSharedFields++;
                } else {
                    sharedFields++;
                }
                leftFields = new HashSet<>(methods.get(left).accessedFields());
            }
        }

        return Math.max(0, noSharedFields - sharedFields);
    }

    private Map<String, Integer> numberOfChildrenByType(List<JavaTypeInfo> types, TypeIndex typeIndex) {
        Map<String, Integer> childrenByType = new HashMap<>();
        for (JavaTypeInfo type : types) {
            typeIndex.resolve(type.packageName(), type.superClassName())
                    .ifPresent(parent -> childrenByType.merge(parent.qualifiedName(), 1, Integer::sum));
        }
        return childrenByType;
    }

    private Map<String, Integer> inheritanceDepthByType(List<JavaTypeInfo> types, TypeIndex typeIndex) {
        Map<String, Integer> depthByType = new HashMap<>();
        for (JavaTypeInfo type : types) {
            depthByType.put(type.qualifiedName(), depth(type, typeIndex, depthByType, new HashSet<>()));
        }
        return depthByType;
    }

    private int depth(
            JavaTypeInfo type,
            TypeIndex typeIndex,
            Map<String, Integer> depthByType,
            Set<String> visiting
    ) {
        if (type.superClassName() == null || type.superClassName().isBlank()) {
            return 0;
        }
        if ("Object".equals(type.superClassName()) || "java.lang.Object".equals(type.superClassName())) {
            return 0;
        }
        if (depthByType.containsKey(type.qualifiedName())) {
            return depthByType.get(type.qualifiedName());
        }
        if (!visiting.add(type.qualifiedName())) {
            return 0;
        }

        return typeIndex.resolve(type.packageName(), type.superClassName())
                .map(parent -> 1 + depth(parent, typeIndex, depthByType, visiting))
                .orElse(1);
    }

    private String riskLevel(
            JavaTypeInfo type,
            Map<String, Integer> inheritanceDepthByType,
            Map<String, Integer> numberOfChildrenByType
    ) {
        int score = 0;
        if (wmc(type) >= 20) {
            score++;
        }
        if (inheritanceDepthByType.getOrDefault(type.qualifiedName(), 0) >= 5) {
            score++;
        }
        if (numberOfChildrenByType.getOrDefault(type.qualifiedName(), 0) >= 6) {
            score++;
        }
        if (cbo(type) >= 8) {
            score++;
        }
        if (rfc(type) >= 20) {
            score++;
        }
        if (lcom(type) >= 5) {
            score++;
        }

        if (score >= 3) {
            return "高风险";
        }
        if (score >= 1) {
            return "中风险";
        }
        return "低风险";
    }

    private boolean isIgnoredDependency(JavaTypeInfo type, String dependency) {
        if (dependency == null || dependency.isBlank()) {
            return true;
        }
        String simpleDependency = simpleName(dependency);
        return dependency.equals(type.name())
                || dependency.equals(type.qualifiedName())
                || simpleDependency.equals(type.name())
                || IGNORED_DEPENDENCIES.contains(simpleDependency)
                || simpleDependency.chars().allMatch(Character::isLowerCase);
    }

    private String simpleName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        }
        int genericStart = name.indexOf('<');
        if (genericStart >= 0) {
            return name.substring(0, genericStart);
        }
        return name;
    }

    private static final class TypeIndex {

        private final Map<String, JavaTypeInfo> byQualifiedName;
        private final Map<String, JavaTypeInfo> bySimpleName;

        private TypeIndex(List<JavaTypeInfo> types) {
            this.byQualifiedName = types.stream()
                    .collect(Collectors.toMap(JavaTypeInfo::qualifiedName, type -> type, (left, right) -> left));
            this.bySimpleName = types.stream()
                    .collect(Collectors.toMap(JavaTypeInfo::name, type -> type, (left, right) -> left));
        }

        private java.util.Optional<JavaTypeInfo> resolve(String packageName, String typeName) {
            if (typeName == null || typeName.isBlank()) {
                return java.util.Optional.empty();
            }
            JavaTypeInfo byFullName = byQualifiedName.get(typeName);
            if (byFullName != null) {
                return java.util.Optional.of(byFullName);
            }

            String samePackageName = packageName == null || packageName.isBlank()
                    ? typeName
                    : packageName + "." + typeName;
            JavaTypeInfo bySamePackageName = byQualifiedName.get(samePackageName);
            if (bySamePackageName != null) {
                return java.util.Optional.of(bySamePackageName);
            }

            return java.util.Optional.ofNullable(bySimpleName.get(typeName));
        }
    }
}

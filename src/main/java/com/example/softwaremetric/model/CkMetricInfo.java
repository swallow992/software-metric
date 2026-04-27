package com.example.softwaremetric.model;

public record CkMetricInfo(
        int weightedMethodsPerClass,
        int depthOfInheritanceTree,
        int numberOfChildren,
        int couplingBetweenObjects,
        int responseForClass,
        int lackOfCohesionInMethods,
        String riskLevel
) {

    public static CkMetricInfo empty() {
        return new CkMetricInfo(0, 0, 0, 0, 0, 0, "未计算");
    }
}

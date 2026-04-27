package com.example.softwaremetric.model;

public record EstimationResult(
        int unadjustedFunctionPoints,
        double valueAdjustmentFactor,
        double functionPoints,
        int actorWeight,
        int useCaseWeight,
        int unadjustedUseCasePoints,
        double useCasePoints,
        double useCaseEffortHours,
        double useCaseEffortPersonMonths,
        String cocomoModeName,
        double cocomoEffortPersonMonths,
        double developmentTimeMonths,
        double averageStaff,
        double estimatedCost
) {
}

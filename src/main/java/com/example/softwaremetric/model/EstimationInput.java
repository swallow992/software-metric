package com.example.softwaremetric.model;

public record EstimationInput(
        int externalInputs,
        int externalOutputs,
        int externalInquiries,
        int internalLogicalFiles,
        int externalInterfaceFiles,
        int technicalComplexityDegree,
        int simpleActors,
        int averageActors,
        int complexActors,
        int simpleUseCases,
        int averageUseCases,
        int complexUseCases,
        double useCaseTechnicalFactor,
        double useCaseEnvironmentalFactor,
        double hoursPerUseCasePoint,
        double sourceKloc,
        String cocomoMode,
        double personMonthCost
) {

    public static EstimationInput defaults() {
        return new EstimationInput(
                4,
                3,
                2,
                2,
                1,
                35,
                1,
                1,
                1,
                2,
                2,
                1,
                1.0,
                1.0,
                20.0,
                5.0,
                "organic",
                12000.0
        );
    }
}

package com.example.softwaremetric.service;

import com.example.softwaremetric.model.EstimationInput;
import com.example.softwaremetric.model.EstimationResult;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EstimationService {

    private static final int EI_WEIGHT = 4;
    private static final int EO_WEIGHT = 5;
    private static final int EQ_WEIGHT = 4;
    private static final int ILF_WEIGHT = 10;
    private static final int EIF_WEIGHT = 7;

    public EstimationResult estimate(EstimationInput input) {
        validate(input);

        int unadjustedFunctionPoints = input.externalInputs() * EI_WEIGHT
                + input.externalOutputs() * EO_WEIGHT
                + input.externalInquiries() * EQ_WEIGHT
                + input.internalLogicalFiles() * ILF_WEIGHT
                + input.externalInterfaceFiles() * EIF_WEIGHT;
        double valueAdjustmentFactor = 0.65 + 0.01 * input.technicalComplexityDegree();
        double functionPoints = unadjustedFunctionPoints * valueAdjustmentFactor;

        int actorWeight = input.simpleActors()
                + input.averageActors() * 2
                + input.complexActors() * 3;
        int useCaseWeight = input.simpleUseCases() * 5
                + input.averageUseCases() * 10
                + input.complexUseCases() * 15;
        int unadjustedUseCasePoints = actorWeight + useCaseWeight;
        double useCasePoints = unadjustedUseCasePoints
                * input.useCaseTechnicalFactor()
                * input.useCaseEnvironmentalFactor();
        double useCaseEffortHours = useCasePoints * input.hoursPerUseCasePoint();
        double useCaseEffortPersonMonths = useCaseEffortHours / 160.0;

        CocomoMode mode = CocomoMode.from(input.cocomoMode());
        double effortPersonMonths = mode.a * Math.pow(input.sourceKloc(), mode.b);
        double developmentTimeMonths = mode.c * Math.pow(effortPersonMonths, mode.d);
        double averageStaff = developmentTimeMonths == 0 ? 0 : effortPersonMonths / developmentTimeMonths;
        double estimatedCost = effortPersonMonths * input.personMonthCost();

        return new EstimationResult(
                unadjustedFunctionPoints,
                valueAdjustmentFactor,
                functionPoints,
                actorWeight,
                useCaseWeight,
                unadjustedUseCasePoints,
                useCasePoints,
                useCaseEffortHours,
                useCaseEffortPersonMonths,
                mode.displayName,
                effortPersonMonths,
                developmentTimeMonths,
                averageStaff,
                estimatedCost
        );
    }

    private void validate(EstimationInput input) {
        if (input == null) {
            throw new IllegalArgumentException("估算参数不能为空。");
        }
        if (input.technicalComplexityDegree() < 0 || input.technicalComplexityDegree() > 70) {
            throw new IllegalArgumentException("功能点技术复杂度调整值应在 0 到 70 之间。");
        }
        if (input.useCaseTechnicalFactor() <= 0 || input.useCaseEnvironmentalFactor() <= 0) {
            throw new IllegalArgumentException("用例点技术因子和环境因子必须大于 0。");
        }
        if (input.hoursPerUseCasePoint() <= 0) {
            throw new IllegalArgumentException("每用例点工时必须大于 0。");
        }
        if (input.sourceKloc() <= 0) {
            throw new IllegalArgumentException("KLOC 必须大于 0。");
        }
        if (input.personMonthCost() < 0) {
            throw new IllegalArgumentException("人月成本不能为负数。");
        }
        if (hasNegativeCount(input)) {
            throw new IllegalArgumentException("数量类参数不能为负数。");
        }
    }

    private boolean hasNegativeCount(EstimationInput input) {
        return input.externalInputs() < 0
                || input.externalOutputs() < 0
                || input.externalInquiries() < 0
                || input.internalLogicalFiles() < 0
                || input.externalInterfaceFiles() < 0
                || input.simpleActors() < 0
                || input.averageActors() < 0
                || input.complexActors() < 0
                || input.simpleUseCases() < 0
                || input.averageUseCases() < 0
                || input.complexUseCases() < 0;
    }

    private enum CocomoMode {
        ORGANIC("organic", "有机型", 2.4, 1.05, 2.5, 0.38),
        SEMI_DETACHED("semi", "半分离型", 3.0, 1.12, 2.5, 0.35),
        EMBEDDED("embedded", "嵌入型", 3.6, 1.20, 2.5, 0.32);

        private final String code;
        private final String displayName;
        private final double a;
        private final double b;
        private final double c;
        private final double d;

        CocomoMode(String code, String displayName, double a, double b, double c, double d) {
            this.code = code;
            this.displayName = displayName;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        private static CocomoMode from(String code) {
            String normalized = code == null ? "" : code.toLowerCase(Locale.ROOT);
            for (CocomoMode mode : values()) {
                if (mode.code.equals(normalized)) {
                    return mode;
                }
            }
            return ORGANIC;
        }
    }
}

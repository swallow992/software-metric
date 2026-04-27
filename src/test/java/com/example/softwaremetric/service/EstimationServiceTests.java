package com.example.softwaremetric.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import com.example.softwaremetric.model.EstimationInput;
import com.example.softwaremetric.model.EstimationResult;
import org.junit.jupiter.api.Test;

class EstimationServiceTests {

    private final EstimationService estimationService = new EstimationService();

    @Test
    void estimatesDefaultProject() {
        EstimationResult result = estimationService.estimate(EstimationInput.defaults());

        assertThat(result.unadjustedFunctionPoints()).isEqualTo(66);
        assertThat(result.valueAdjustmentFactor()).isCloseTo(1.0, offset(0.001));
        assertThat(result.functionPoints()).isCloseTo(66.0, offset(0.001));
        assertThat(result.actorWeight()).isEqualTo(6);
        assertThat(result.useCaseWeight()).isEqualTo(45);
        assertThat(result.unadjustedUseCasePoints()).isEqualTo(51);
        assertThat(result.useCasePoints()).isCloseTo(51.0, offset(0.001));
        assertThat(result.useCaseEffortHours()).isCloseTo(1020.0, offset(0.001));
        assertThat(result.useCaseEffortPersonMonths()).isCloseTo(6.375, offset(0.001));
        assertThat(result.cocomoModeName()).isEqualTo("有机型");
        assertThat(result.cocomoEffortPersonMonths()).isCloseTo(13.005, offset(0.001));
        assertThat(result.developmentTimeMonths()).isCloseTo(6.626, offset(0.001));
        assertThat(result.averageStaff()).isCloseTo(1.962, offset(0.001));
        assertThat(result.estimatedCost()).isCloseTo(156066.96, offset(0.01));
    }

    @Test
    void rejectsInvalidKloc() {
        EstimationInput input = new EstimationInput(
                0, 0, 0, 0, 0, 35,
                0, 0, 0, 0, 0, 0,
                1.0, 1.0, 20.0,
                0.0, "organic", 12000.0
        );

        assertThatThrownBy(() -> estimationService.estimate(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KLOC");
    }
}

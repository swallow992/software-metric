package com.example.softwaremetric.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.data.Offset.offset;

import com.example.softwaremetric.model.JavaMethodInfo;
import com.example.softwaremetric.model.SourceAnalysisResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SourceAnalysisServiceTests {

    @Autowired
    private SourceAnalysisService sourceAnalysisService;

    @Test
    void analyzesSampleJavaProject() {
        SourceAnalysisResult result = sourceAnalysisService.analyze("examples/sample-java-project");

        assertThat(result.javaFileCount()).isEqualTo(3);
        assertThat(result.packageCount()).isEqualTo(1);
        assertThat(result.classCount()).isEqualTo(3);
        assertThat(result.interfaceCount()).isZero();
        assertThat(result.methodCount()).isEqualTo(5);
        assertThat(result.fieldCount()).isEqualTo(1);
        assertThat(result.totalLines()).isEqualTo(53);
        assertThat(result.blankLines()).isEqualTo(11);
        assertThat(result.commentLines()).isZero();
        assertThat(result.codeLines()).isEqualTo(42);
        assertThat(result.totalCyclomaticComplexity()).isEqualTo(10);
        assertThat(result.averageMethodCyclomaticComplexity()).isCloseTo(2.0, offset(0.01));
        assertThat(result.types())
                .extracting(type -> type.qualifiedName())
                .containsExactly(
                        "com.example.sample.AdvancedCalculator",
                        "com.example.sample.Calculator",
                        "com.example.sample.OrderService"
                );
        assertThat(result.types())
                .flatExtracting(type -> type.methods())
                .extracting(JavaMethodInfo::name, JavaMethodInfo::cyclomaticComplexity)
                .containsExactly(
                        tuple("multiply", 1),
                        tuple("lastResult", 2),
                        tuple("add", 1),
                        tuple("divide", 2),
                        tuple("countPayableOrders", 4)
                );
        assertThat(result.types())
                .extracting(
                        type -> type.qualifiedName(),
                        type -> type.ckMetric().weightedMethodsPerClass(),
                        type -> type.ckMetric().depthOfInheritanceTree(),
                        type -> type.ckMetric().numberOfChildren(),
                        type -> type.ckMetric().couplingBetweenObjects(),
                        type -> type.ckMetric().responseForClass(),
                        type -> type.ckMetric().lackOfCohesionInMethods(),
                        type -> type.ckMetric().riskLevel()
                )
                .containsExactly(
                        tuple("com.example.sample.AdvancedCalculator", 3, 1, 0, 3, 6, 0, "低风险"),
                        tuple("com.example.sample.Calculator", 3, 0, 1, 1, 3, 0, "低风险"),
                        tuple("com.example.sample.OrderService", 4, 0, 0, 1, 1, 0, "低风险")
                );
    }
}

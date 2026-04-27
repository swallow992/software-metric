package com.example.softwaremetric.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import com.example.softwaremetric.model.UseCaseModelInput;
import com.example.softwaremetric.model.UseCaseModelResult;
import org.junit.jupiter.api.Test;

class UseCaseModelServiceTests {

    private final UseCaseModelService useCaseModelService = new UseCaseModelService();

    @Test
    void calculatesUseCasePointsFromStructuredInput() {
        UseCaseModelInput input = new UseCaseModelInput(
                """
                普通用户, 复杂
                支付平台, 一般
                通知服务, 简单
                """,
                """
                注册账号, 简单
                提交订单, 一般
                支付订单, 复杂
                """,
                1.0,
                1.0,
                20.0
        );

        UseCaseModelResult result = useCaseModelService.calculate(input);

        assertThat(result.simpleActorCount()).isEqualTo(1);
        assertThat(result.averageActorCount()).isEqualTo(1);
        assertThat(result.complexActorCount()).isEqualTo(1);
        assertThat(result.simpleUseCaseCount()).isEqualTo(1);
        assertThat(result.averageUseCaseCount()).isEqualTo(1);
        assertThat(result.complexUseCaseCount()).isEqualTo(1);
        assertThat(result.actorWeight()).isEqualTo(6);
        assertThat(result.useCaseWeight()).isEqualTo(30);
        assertThat(result.unadjustedUseCasePoints()).isEqualTo(36);
        assertThat(result.useCasePoints()).isCloseTo(36.0, offset(0.001));
        assertThat(result.effortHours()).isCloseTo(720.0, offset(0.001));
        assertThat(result.effortPersonMonths()).isCloseTo(4.5, offset(0.001));
    }
}

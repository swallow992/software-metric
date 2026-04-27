package com.example.softwaremetric.model;

public record UseCaseModelInput(
        String actorsText,
        String useCasesText,
        double technicalFactor,
        double environmentalFactor,
        double hoursPerUseCasePoint
) {

    public static UseCaseModelInput defaults() {
        return new UseCaseModelInput(
                """
                普通用户, 复杂
                支付平台, 一般
                通知服务, 简单
                """,
                """
                注册账号, 简单
                提交订单, 一般
                支付订单, 复杂
                查看订单状态, 简单
                """,
                1.0,
                1.0,
                20.0
        );
    }
}

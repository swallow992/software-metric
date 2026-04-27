package com.example.softwaremetric.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectOverviewService {

    public String currentPhase() {
        return "基础版完成";
    }

    public List<String> nextSteps() {
        return List.of(
                "源码分析与 CK 指标已完成",
                "源码目录分析和 zip 源码包上传已完成",
                "功能点、用例点和 COCOMO 估算已完成",
                "用例模型结构化输入和 UCP 折算已完成",
                "源码报告、估算报告、PDF 导出和 ECharts 可视化已完成",
                "README、系统设计、使用说明和测试说明已持续更新"
        );
    }
}

package com.example.softwaremetric.report;

import com.example.softwaremetric.model.EstimationInput;
import com.example.softwaremetric.model.EstimationResult;
import java.text.DecimalFormat;
import org.springframework.stereotype.Service;

@Service
public class EstimationReportService {

    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");

    public String toMarkdown(EstimationInput input, EstimationResult result) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# 软件项目估算报告\n\n");

        appendFunctionPointSection(markdown, input, result);
        appendUseCasePointSection(markdown, input, result);
        appendCocomoSection(markdown, input, result);
        appendExplanation(markdown);

        return markdown.toString();
    }

    private void appendFunctionPointSection(StringBuilder markdown, EstimationInput input, EstimationResult result) {
        markdown.append("## 功能点估算\n\n");
        markdown.append("| 指标 | 数值 |\n");
        markdown.append("| --- | ---: |\n");
        appendRow(markdown, "外部输入 EI", input.externalInputs());
        appendRow(markdown, "外部输出 EO", input.externalOutputs());
        appendRow(markdown, "外部查询 EQ", input.externalInquiries());
        appendRow(markdown, "内部逻辑文件 ILF", input.internalLogicalFiles());
        appendRow(markdown, "外部接口文件 EIF", input.externalInterfaceFiles());
        appendRow(markdown, "技术复杂度调整值", input.technicalComplexityDegree());
        appendRow(markdown, "未调整功能点 UFP", result.unadjustedFunctionPoints());
        appendRow(markdown, "调整系数 VAF", result.valueAdjustmentFactor());
        appendRow(markdown, "功能点 FP", result.functionPoints());
        markdown.append('\n');
    }

    private void appendUseCasePointSection(StringBuilder markdown, EstimationInput input, EstimationResult result) {
        markdown.append("## 用例点估算\n\n");
        markdown.append("| 指标 | 数值 |\n");
        markdown.append("| --- | ---: |\n");
        appendRow(markdown, "简单参与者", input.simpleActors());
        appendRow(markdown, "一般参与者", input.averageActors());
        appendRow(markdown, "复杂参与者", input.complexActors());
        appendRow(markdown, "简单用例", input.simpleUseCases());
        appendRow(markdown, "一般用例", input.averageUseCases());
        appendRow(markdown, "复杂用例", input.complexUseCases());
        appendRow(markdown, "技术因子", input.useCaseTechnicalFactor());
        appendRow(markdown, "环境因子", input.useCaseEnvironmentalFactor());
        appendRow(markdown, "每用例点工时", input.hoursPerUseCasePoint());
        appendRow(markdown, "参与者权重", result.actorWeight());
        appendRow(markdown, "用例权重", result.useCaseWeight());
        appendRow(markdown, "未调整用例点 UUCP", result.unadjustedUseCasePoints());
        appendRow(markdown, "用例点 UCP", result.useCasePoints());
        appendRow(markdown, "用例工作量（小时）", result.useCaseEffortHours());
        appendRow(markdown, "用例工作量（人月）", result.useCaseEffortPersonMonths());
        markdown.append('\n');
    }

    private void appendCocomoSection(StringBuilder markdown, EstimationInput input, EstimationResult result) {
        markdown.append("## COCOMO 估算\n\n");
        markdown.append("| 指标 | 数值 |\n");
        markdown.append("| --- | ---: |\n");
        appendRow(markdown, "规模 KLOC", input.sourceKloc());
        appendTextRow(markdown, "项目类型", result.cocomoModeName());
        appendRow(markdown, "人月成本", input.personMonthCost());
        appendRow(markdown, "COCOMO 工作量（人月）", result.cocomoEffortPersonMonths());
        appendRow(markdown, "开发工期（月）", result.developmentTimeMonths());
        appendRow(markdown, "平均人员", result.averageStaff());
        appendRow(markdown, "估算成本", result.estimatedCost());
        markdown.append('\n');
    }

    private void appendExplanation(StringBuilder markdown) {
        markdown.append("## 计算说明\n\n");
        markdown.append("- 功能点 FP = 未调整功能点 UFP × 调整系数 VAF。\n");
        markdown.append("- 用例点 UCP = 未调整用例点 UUCP × 技术因子 × 环境因子。\n");
        markdown.append("- 用例工作量按每用例点工时折算，并以 160 小时折算为 1 人月。\n");
        markdown.append("- COCOMO 使用基础模型，根据项目类型选择对应参数估算工作量和工期。\n");
    }

    private void appendRow(StringBuilder markdown, String label, int value) {
        markdown.append("| ").append(label).append(" | ").append(value).append(" |\n");
    }

    private void appendRow(StringBuilder markdown, String label, double value) {
        markdown.append("| ").append(label).append(" | ").append(TWO_DECIMALS.format(value)).append(" |\n");
    }

    private void appendTextRow(StringBuilder markdown, String label, String value) {
        markdown.append("| ").append(label).append(" | ").append(value).append(" |\n");
    }
}

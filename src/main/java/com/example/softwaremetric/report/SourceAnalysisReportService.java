package com.example.softwaremetric.report;

import com.example.softwaremetric.model.JavaMethodInfo;
import com.example.softwaremetric.model.JavaTypeInfo;
import com.example.softwaremetric.model.SourceAnalysisResult;
import com.example.softwaremetric.model.SourceFileMetric;
import java.text.DecimalFormat;
import org.springframework.stereotype.Service;

@Service
public class SourceAnalysisReportService {

    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");

    public String toMarkdown(SourceAnalysisResult result) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# 软件度量分析报告\n\n");
        markdown.append("## 项目概览\n\n");
        markdown.append("- 源码目录：").append(result.sourceRoot()).append('\n');
        markdown.append("- Java 文件数：").append(result.javaFileCount()).append('\n');
        markdown.append("- 包数量：").append(result.packageCount()).append('\n');
        markdown.append("- 类数量：").append(result.classCount()).append('\n');
        markdown.append("- 接口数量：").append(result.interfaceCount()).append('\n');
        markdown.append("- 方法数量：").append(result.methodCount()).append('\n');
        markdown.append("- 字段数量：").append(result.fieldCount()).append("\n\n");

        markdown.append("## 代码规模与复杂度\n\n");
        markdown.append("- 总行数：").append(result.totalLines()).append('\n');
        markdown.append("- 有效代码行：").append(result.codeLines()).append('\n');
        markdown.append("- 注释行：").append(result.commentLines()).append('\n');
        markdown.append("- 空行：").append(result.blankLines()).append('\n');
        markdown.append("- 总圈复杂度：").append(result.totalCyclomaticComplexity()).append('\n');
        markdown.append("- 平均方法复杂度：")
                .append(TWO_DECIMALS.format(result.averageMethodCyclomaticComplexity()))
                .append("\n\n");

        appendFileMetricTable(markdown, result);
        appendTypeMetricTable(markdown, result);
        appendMethodComplexityTable(markdown, result);

        markdown.append("## 指标说明\n\n");
        markdown.append("- WMC：类中方法圈复杂度之和。\n");
        markdown.append("- DIT：继承树深度。\n");
        markdown.append("- NOC：直接子类数量。\n");
        markdown.append("- CBO：类与其他类型的耦合数量。\n");
        markdown.append("- RFC：类可响应的方法集合数量。\n");
        markdown.append("- LCOM：方法缺少共享字段访问的程度。\n");
        return markdown.toString();
    }

    private void appendFileMetricTable(StringBuilder markdown, SourceAnalysisResult result) {
        markdown.append("## 文件行数明细\n\n");
        markdown.append("| 文件 | 总行数 | 有效代码行 | 注释行 | 空行 |\n");
        markdown.append("| --- | ---: | ---: | ---: | ---: |\n");
        for (SourceFileMetric fileMetric : result.fileMetrics()) {
            markdown.append("| ")
                    .append(fileMetric.sourceFile())
                    .append(" | ")
                    .append(fileMetric.totalLines())
                    .append(" | ")
                    .append(fileMetric.codeLines())
                    .append(" | ")
                    .append(fileMetric.commentLines())
                    .append(" | ")
                    .append(fileMetric.blankLines())
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void appendTypeMetricTable(StringBuilder markdown, SourceAnalysisResult result) {
        markdown.append("## 类级 CK 指标\n\n");
        markdown.append("| 类型 | WMC | DIT | NOC | CBO | RFC | LCOM | 风险 |\n");
        markdown.append("| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |\n");
        for (JavaTypeInfo type : result.types()) {
            markdown.append("| ")
                    .append(type.qualifiedName())
                    .append(" | ")
                    .append(type.ckMetric().weightedMethodsPerClass())
                    .append(" | ")
                    .append(type.ckMetric().depthOfInheritanceTree())
                    .append(" | ")
                    .append(type.ckMetric().numberOfChildren())
                    .append(" | ")
                    .append(type.ckMetric().couplingBetweenObjects())
                    .append(" | ")
                    .append(type.ckMetric().responseForClass())
                    .append(" | ")
                    .append(type.ckMetric().lackOfCohesionInMethods())
                    .append(" | ")
                    .append(type.ckMetric().riskLevel())
                    .append(" |\n");
        }
        markdown.append('\n');
    }

    private void appendMethodComplexityTable(StringBuilder markdown, SourceAnalysisResult result) {
        markdown.append("## 方法复杂度明细\n\n");
        markdown.append("| 类型 | 方法 | 返回类型 | 圈复杂度 |\n");
        markdown.append("| --- | --- | --- | ---: |\n");
        for (JavaTypeInfo type : result.types()) {
            for (JavaMethodInfo method : type.methods()) {
                markdown.append("| ")
                        .append(type.qualifiedName())
                        .append(" | ")
                        .append(method.name())
                        .append(" | ")
                        .append(method.returnType())
                        .append(" | ")
                        .append(method.cyclomaticComplexity())
                        .append(" |\n");
            }
        }
        markdown.append('\n');
    }
}

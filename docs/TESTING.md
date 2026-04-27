# 测试说明

## 1. 运行测试

```powershell
mvn test
```

## 2. 当前测试覆盖

测试文件：

- `SoftwareMetricApplicationTests`
- `SourceLineCounterTests`
- `SourceAnalysisServiceTests`
- `EstimationServiceTests`
- `ReportControllerTests`

覆盖范围：

- Spring Boot 应用上下文启动
- LoC 行数统计
- Java 示例项目解析
- zip 源码包上传、解压和分析
- 圈复杂度计算
- CK 指标计算
- ECharts 图表容器渲染
- 功能点、用例点、COCOMO 估算
- 用例模型结构化输入和 UCP 折算
- Markdown 报告导出
- HTML 报告渲染
- 估算报告 HTML 渲染和 Markdown 导出
- 源码报告和估算报告 PDF 导出

## 3. 验收命令

```powershell
mvn test
mvn package -DskipTests
```

## 4. 手工验证路径

```text
http://localhost:8080/
http://localhost:8080/analyze
http://localhost:8080/estimate
http://localhost:8080/use-case-model
```

源码分析示例输入：

```text
examples/sample-java-project
```

源码分析页也可以上传 `.zip` 源码包进行手工验证。

报告接口：

```text
http://localhost:8080/reports/source/html?sourcePath=examples/sample-java-project
http://localhost:8080/reports/source/markdown?sourcePath=examples/sample-java-project
http://localhost:8080/reports/source/pdf?sourcePath=examples/sample-java-project
```

估算报告建议通过 `/estimate` 页面计算后点击报告按钮验证，页面会自动携带当前估算参数。

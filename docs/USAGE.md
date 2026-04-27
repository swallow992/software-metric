# 使用说明

## 1. 启动项目

```powershell
mvn spring-boot:run
```

访问：

```text
http://localhost:8080/
```

## 2. 源码分析

打开：

```text
http://localhost:8080/analyze
```

输入 Java 源码目录，例如：

```text
examples/sample-java-project
```

也可以在同一页面上传包含 Java 源码的 `.zip` 包，系统会自动解压到本地上传目录并扫描其中的 `.java` 文件。

点击“分析源码”或“上传 zip 并分析”后，页面会显示：

- Java 文件数量
- 包、类、接口、方法、字段数量
- 总行数、有效代码行、注释行、空行
- 总圈复杂度和平均方法复杂度
- 文件行数明细
- 代码行分布、文件有效代码行、类级 CK 指标和风险等级图表
- 类、方法、字段明细
- CK 指标和风险等级

## 3. 导出报告

源码分析完成后，结果区域提供：

- HTML 报告
- Markdown 报告
- PDF 报告

HTML 报告包含可视化图表，适合浏览器预览和演示；Markdown 报告适合课程报告引用和二次编辑；PDF 报告适合提交和归档。

## 4. 项目估算

打开：

```text
http://localhost:8080/estimate
```

填写：

- 功能点参数：EI、EO、EQ、ILF、EIF、技术复杂度调整值
- 用例点参数：参与者、用例、技术因子、环境因子、每用例点工时
- COCOMO 参数：KLOC、项目类型、人月成本

点击“计算估算”后，页面会输出：

- 未调整功能点
- 功能点 FP
- 未调整用例点
- 用例点 UCP
- 用例工作量
- COCOMO 工作量
- 开发工期
- 平均人员
- 估算成本

估算结果区域也提供：

- HTML 报告
- Markdown 报告
- PDF 报告

估算报告会保留输入参数、FP、UCP、COCOMO 工作量、工期、人员和成本结果。

## 5. 示例项目

仓库内置示例项目：

```text
examples/sample-java-project
```

该项目包含继承、字段、方法调用、循环和条件判断，可用于演示 LoC、圈复杂度和 CK 指标。

## 6. 用例模型

打开：

```text
http://localhost:8080/use-case-model
```

参与者和用例每行填写一个条目，格式为：

```text
名称, 简单
名称, 一般
名称, 复杂
```

页面会输出参与者权重、用例权重、未调整用例点、UCP、工作量小时和工作量人月，也可以将结果带入项目估算页面继续计算成本和工期。

## 7. 上传目录

上传的 zip 源码包会解压到：

```text
uploads/source-zips
```

该目录已加入 `.gitignore`，不会作为项目源码提交。

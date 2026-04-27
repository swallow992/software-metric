# 系统设计文档

## 1. 系统概述

software-metric 是一个基于 Spring Boot 的软件度量自动化工具。系统以 Java 源码、用例模型和用户输入参数为主要输入，输出源码结构、代码规模、复杂度、CK 面向对象度量、功能点、用例点、COCOMO 估算和报告。

## 2. 架构设计

系统采用分层结构：

```text
Controller 层
    ↓
Service 层
    ↓
Parser / Metric / Report 层
    ↓
Model 层
```

主要模块：

- `controller`：页面入口、表单提交、报告导出。
- `service`：组织源码分析、zip 上传解压、用例模型折算、项目估算等业务流程。
- `parser`：基于 Eclipse JDT ASTParser 解析 Java 源码。
- `metric`：计算 LoC、圈复杂度和 CK 指标。
- `report`：生成源码分析和项目估算的 Markdown、HTML、PDF 报告内容。
- `static/js`：初始化 ECharts 图表，展示代码行、文件规模、CK 指标和风险分布。
- `model`：保存输入参数、分析结果、估算结果和指标对象。

## 3. 源码分析流程

```text
用户输入源码目录或上传 zip 源码包
    ↓
zip 源码包解压到本地上传目录
    ↓
扫描 .java 文件
    ↓
读取源码文本
    ↓
统计 LoC
    ↓
JDT ASTParser 解析
    ↓
提取类、方法、字段、继承、依赖、调用、字段访问
    ↓
计算圈复杂度和 CK 指标
    ↓
页面展示或报告导出
```

## 4. 估算流程

```text
用户输入 FP / UCP / COCOMO 参数
    ↓
参数校验
    ↓
功能点计算
    ↓
用例点计算
    ↓
COCOMO 工作量、工期、人员、成本计算
    ↓
页面展示估算结果
```

用例模型结构化输入流程：

```text
用户输入参与者和用例清单
    ↓
解析名称与复杂度
    ↓
折算参与者权重和用例权重
    ↓
计算 UCP 和用例工作量
    ↓
带入项目估算模型
```

## 5. 指标说明

### 5.1 LoC

- 总行数：源码文件总行数。
- 有效代码行：包含实际代码的行。
- 注释行：包含单行注释或块注释的行。
- 空行：不包含可见字符的行。

### 5.2 圈复杂度

每个方法基础复杂度为 1。遇到以下结构时增加复杂度：

- `if`
- `for`
- `while`
- `do while`
- `case`
- `catch`
- 三元表达式
- 条件与 `&&`
- 条件或 `||`

### 5.3 CK 指标

- WMC：类中方法圈复杂度之和。
- DIT：继承树深度。
- NOC：直接子类数量。
- CBO：类与其他类型的耦合数量。
- RFC：类可响应的方法集合数量。
- LCOM：方法缺少共享字段访问的程度。

## 6. 报告设计

源码分析完成后，系统支持：

- zip 源码包上传分析：`/analyze/upload`
- HTML 报告预览：`/reports/source/html`
- Markdown 报告下载：`/reports/source/markdown`
- PDF 报告下载：`/reports/source/pdf`
- 估算 HTML 报告预览：`/reports/estimate/html`
- 估算 Markdown 报告下载：`/reports/estimate/markdown`
- 估算 PDF 报告下载：`/reports/estimate/pdf`
- ECharts 图表：分析结果页和 HTML 报告页内嵌展示

报告包含项目概览、代码规模、复杂度、文件行数明细、类级 CK 指标和方法复杂度明细。

# software-metric

`software-metric` 是一个面向 Java 项目的软件度量自动化工具。系统通过 Web 页面接收 Java 源码目录、源码 zip 包和用户输入的估算参数，自动完成源码规模统计、圈复杂度计算、CK 面向对象度量、功能点估算、用例点估算、COCOMO 工作量/工期/成本估算，并支持图表展示和 HTML、Markdown、PDF 报告导出。

这个项目按照软件度量课程/实验项目的要求设计，目标不是只做一个单点计算器，而是把软件生命周期中常见的度量对象串起来：源码、类结构、方法复杂度、对象耦合、用例模型、项目规模、工作量、成本、工期和人员规模。

## 项目目标

- 对 Java 源码进行自动化结构分析，降低手工统计代码规模和类信息的成本。
- 实现传统代码度量，包括总代码行、有效代码行、注释行、空行和圈复杂度。
- 实现 CK 面向对象度量模型中的核心指标，包括 WMC、DIT、NOC、CBO、RFC、LCOM。
- 支持功能点、用例点和 COCOMO 估算，覆盖规模、工作量、成本、工期和人员等软件项目管理指标。
- 提供 Web 页面、图表、示例项目和报告导出能力，便于展示、实验验收和后续扩展。

## 已实现功能

### 1. Java 源码分析

系统支持两种源码输入方式：

- 在页面中输入本机 Java 源码目录路径。
- 上传包含 `.java` 文件的 `.zip` 源码包。

分析完成后会输出：

- Java 文件数量。
- 包数量。
- 类、接口、枚举等类型信息。
- 方法数量、字段数量。
- 总行数、有效代码行、注释行、空行。
- 项目总圈复杂度和方法平均圈复杂度。
- 文件级代码行明细。
- 类级 CK 指标和风险等级。
- 方法级参数、返回值、修饰符、调用方法、访问字段和圈复杂度。

### 2. zip 源码包上传与安全解压

上传入口为 `/analyze/upload`。系统只接受 `.zip` 文件，上传后会解压到本地 `uploads/source-zips` 目录下的独立 UUID 子目录。

解压实现中做了两个安全处理：

- 将 zip entry 中的 Windows 反斜杠路径统一转换为 `/`，保证跨平台路径兼容。
- 对每个解压后的目标路径执行 `normalize()` 并检查是否仍位于目标目录内，避免 zip slip 路径穿越问题。

相关实现：

- `ZipSourceUploadService`：负责上传文件校验、创建解压目录、逐项解压和路径安全检查。
- `HomeController`：提供 `/analyze/upload` 表单入口，并在解压后直接调用源码分析服务。

### 3. Eclipse JDT ASTParser 源码解析

Java 源码结构解析基于 Eclipse JDT Core 的 `ASTParser`，解析级别使用 Java 17。

系统会对每个 `.java` 文件创建 `CompilationUnit`，并通过 AST Visitor 提取：

- 包名。
- 类、接口、枚举名称。
- 父类和实现接口。
- 字段名称、类型、修饰符。
- 方法名称、返回类型、参数、修饰符。
- 类型依赖。
- 方法调用。
- 字段访问。

相关实现：

- `JavaSourceParser`：封装 JDT ASTParser，负责将源码文件转换为项目内部的 `JavaTypeInfo`、`JavaMethodInfo`、`JavaFieldInfo`。
- `SourceAnalysisService`：负责扫描目录、读取 UTF-8 源码、调用解析器和指标计算器，最终组装 `SourceAnalysisResult`。

### 4. 代码行 LoC 统计

系统会对源码文本进行逐行扫描，统计：

- `totalLines`：总行数。
- `codeLines`：有效代码行。
- `commentLines`：注释行。
- `blankLines`：空行。

统计结果既会汇总到项目级结果，也会保留到每个源码文件的明细中，用于页面表格和图表展示。

相关实现：

- `SourceLineCounter`：负责代码行、注释行和空行识别。
- `SourceFileMetric`：保存单文件行数度量结果。

### 5. 圈复杂度计算

每个方法的基础圈复杂度为 1。源码解析时，系统会遍历方法体 AST，并在遇到以下结构时增加复杂度：

- `if`
- `for`
- 增强 `for`
- `while`
- `do while`
- `switch case`
- `catch`
- 三元表达式
- 条件与 `&&`
- 条件或 `||`

系统会进一步汇总：

- 方法级圈复杂度。
- 类级圈复杂度总和。
- 项目级圈复杂度总和。
- 方法平均圈复杂度。

相关实现：

- `JavaSourceParser.ComplexityVisitor`：在 AST 上统计方法级圈复杂度。
- `JavaTypeInfo`、`JavaMethodInfo`：保存类型和方法复杂度结果。

### 6. CK 面向对象度量

项目实现了 CK 度量模型中的 6 个核心指标：

| 指标 | 含义 | 当前实现方式 |
| --- | --- | --- |
| WMC | Weighted Methods per Class，类中方法复杂度之和 | 使用类内所有方法的圈复杂度求和 |
| DIT | Depth of Inheritance Tree，继承树深度 | 根据父类关系在当前分析结果中递归计算 |
| NOC | Number of Children，直接子类数量 | 统计以当前类为父类的类型数量 |
| CBO | Coupling Between Objects，对象耦合数 | 根据 AST 中提取的类型依赖去重统计，并过滤常见基础类型 |
| RFC | Response For a Class，类响应集大小 | 类自身方法集合加上方法体内调用的方法集合 |
| LCOM | Lack of Cohesion in Methods，方法内聚缺失度 | 比较方法对字段的访问集合，统计不共享字段的方法对 |

系统还根据 CK 指标给出类级风险等级：

- 低风险
- 中风险
- 高风险

风险判定会综合 WMC、DIT、NOC、CBO、RFC 和 LCOM 的阈值表现，作为代码维护风险提示。

相关实现：

- `CkMetricCalculator`：负责 CK 指标计算、继承关系索引、耦合过滤、响应集统计、LCOM 计算和风险等级判断。
- `CkMetricInfo`：保存 CK 指标和风险等级。

### 7. 项目估算

估算页面入口为 `/estimate`。系统支持三类估算模型：

#### 功能点 FP

输入项包括：

- 外部输入 EI
- 外部输出 EO
- 外部查询 EQ
- 内部逻辑文件 ILF
- 外部接口文件 EIF
- 技术复杂度调整值

计算过程：

1. 按固定权重计算未调整功能点 UFP。
2. 使用 `0.65 + 0.01 * 技术复杂度调整值` 计算调整因子。
3. 得到调整后的功能点 FP。

#### 用例点 UCP

输入项包括：

- 简单、一般、复杂参与者数量。
- 简单、一般、复杂用例数量。
- 技术因子。
- 环境因子。
- 每用例点工时。

计算过程：

1. 参与者按 1、2、3 加权。
2. 用例按 5、10、15 加权。
3. 计算未调整用例点 UUCP。
4. 乘以技术因子和环境因子得到 UCP。
5. 根据每用例点工时换算工作量小时和人月。

#### COCOMO

输入项包括：

- KLOC
- COCOMO 模式：有机型、半分离型、嵌入型。
- 人月成本。

输出结果包括：

- 工作量，人月。
- 开发工期，月。
- 平均人员数。
- 估算成本。

相关实现：

- `EstimationService`：集中实现 FP、UCP 和 COCOMO 公式。
- `EstimationInput`：保存估算输入。
- `EstimationResult`：保存估算输出。
- `EstimateController`：提供 `/estimate` 页面入口和表单提交处理。

### 8. 用例模型结构化输入

用例模型页面入口为 `/use-case-model`。它用于把文本形式的参与者和用例清单转换为 UCP 估算输入。

输入格式示例：

```text
管理员, 复杂
普通用户, 简单
第三方系统, 一般
```

用例输入也采用相同格式：

```text
登录系统, 简单
提交订单, 一般
生成统计报表, 复杂
```

系统支持中文复杂度：`简单`、`一般`、`复杂`，也兼容部分英文别名。计算后会输出：

- 参与者明细和权重。
- 用例明细和权重。
- 简单、一般、复杂项目数量。
- 参与者权重合计。
- 用例权重合计。
- UUCP、UCP。
- 工作量小时和人月。

相关实现：

- `UseCaseModelService`：负责文本解析、复杂度识别、权重换算和 UCP 计算。
- `UseCaseModelInput`、`UseCaseModelItem`、`UseCaseModelResult`：保存输入、解析项和计算结果。
- `UseCaseModelController`：提供 `/use-case-model` 页面入口。

### 9. 可视化图表

系统在源码分析结果页和报告页中使用 ECharts 展示关键指标，包含：

- 代码行分布。
- 文件有效代码行对比。
- CK 指标图表。
- 风险等级分布。

相关实现：

- `src/main/resources/static/js/app.js`：初始化 ECharts 图表。
- `src/main/resources/templates/analyze.html`：源码分析页面。
- `src/main/resources/templates/source-report.html`：源码分析 HTML 报告页面。

### 10. 报告导出

系统支持源码分析报告和项目估算报告导出。

源码分析报告：

- HTML 预览：`/reports/source/html?sourcePath=...`
- Markdown 下载：`/reports/source/markdown?sourcePath=...`
- PDF 下载：`/reports/source/pdf?sourcePath=...`

项目估算报告：

- HTML 预览：`/reports/estimate/html`
- Markdown 下载：`/reports/estimate/markdown`
- PDF 下载：`/reports/estimate/pdf`

报告内容包括：

- 项目概览。
- 代码规模统计。
- 文件级 LoC 明细。
- 类级 CK 指标。
- 方法级复杂度。
- FP、UCP、COCOMO 估算结果。
- 工作量、成本、工期和人员规模。

相关实现：

- `SourceAnalysisReportService`：生成源码分析 Markdown 报告内容。
- `EstimationReportService`：生成估算 Markdown 报告内容。
- `PdfReportService`：基于 OpenPDF 将报告内容转换为 PDF。
- `ReportController`：提供 HTML、Markdown、PDF 报告路由。

## 系统架构

项目采用典型 Spring Boot 分层结构：

```text
Controller 层
    -> Service 层
        -> Parser / Metric / Report 层
            -> Model 层
```

主要模块说明：

| 模块 | 目录 | 作用 |
| --- | --- | --- |
| Controller | `src/main/java/com/example/softwaremetric/controller` | 页面入口、表单提交、报告导出路由 |
| Service | `src/main/java/com/example/softwaremetric/service` | 组织源码分析、zip 解压、估算和用例模型业务流程 |
| Parser | `src/main/java/com/example/softwaremetric/parser` | 基于 JDT ASTParser 解析 Java 源码结构 |
| Metric | `src/main/java/com/example/softwaremetric/metric` | 计算 LoC、圈复杂度和 CK 指标 |
| Report | `src/main/java/com/example/softwaremetric/report` | 生成 Markdown、HTML、PDF 报告内容 |
| Model | `src/main/java/com/example/softwaremetric/model` | 保存输入参数、源码分析结果和估算结果 |
| Templates | `src/main/resources/templates` | Thymeleaf 页面模板 |
| Static | `src/main/resources/static` | CSS 和 JavaScript 静态资源 |
| Tests | `src/test/java/com/example/softwaremetric` | 单元测试和控制器测试 |

## 技术栈

- Java 17
- Spring Boot 3.3.6
- Maven 3.9.9
- Thymeleaf
- Eclipse JDT Core 3.37.0
- ECharts
- OpenPDF 1.3.39
- JUnit 5

## 页面与接口入口

| 功能 | 路径 | 说明 |
| --- | --- | --- |
| 首页 | `/` | 展示项目阶段和下一步计划 |
| 源码分析页面 | `/analyze` | 输入源码目录并分析 |
| 源码目录分析提交 | `POST /analyze` | 分析本机源码目录 |
| zip 上传分析 | `POST /analyze/upload` | 上传 zip 源码包并分析 |
| 项目估算 | `/estimate` | FP、UCP、COCOMO 估算 |
| 用例模型 | `/use-case-model` | 结构化参与者/用例输入并换算 UCP |
| 源码 HTML 报告 | `/reports/source/html` | 浏览器预览源码分析报告 |
| 源码 Markdown 报告 | `/reports/source/markdown` | 下载源码分析 Markdown |
| 源码 PDF 报告 | `/reports/source/pdf` | 下载源码分析 PDF |
| 估算 HTML 报告 | `/reports/estimate/html` | 浏览器预览估算报告 |
| 估算 Markdown 报告 | `/reports/estimate/markdown` | 下载估算 Markdown |
| 估算 PDF 报告 | `/reports/estimate/pdf` | 下载估算 PDF |
| 项目计划文档 | `/docs/PROJECT_PLAN.md` | 读取项目计划 Markdown |

## 快速启动

确保本机已安装 Java 17 和 Maven。

```powershell
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080/
```

如果当前终端没有配置环境变量，可以显式指定：

```powershell
$env:JAVA_HOME='D:\software\dev-tools\jdk-17'
$env:MAVEN_HOME='D:\software\dev-tools\apache-maven-3.9.9'
$env:M2_HOME=$env:MAVEN_HOME
$env:Path="$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:Path"
mvn spring-boot:run
```

## 示例使用流程

### 分析示例 Java 项目

1. 启动应用。
2. 打开 `http://localhost:8080/analyze`。
3. 使用默认示例目录：

```text
examples/sample-java-project
```

4. 点击分析，查看源码概览、文件行数、CK 指标、复杂度明细和图表。
5. 使用页面上的报告入口导出 HTML、Markdown 或 PDF 报告。

### 进行项目估算

1. 打开 `http://localhost:8080/estimate`。
2. 输入功能点、用例点和 COCOMO 参数。
3. 提交后查看 FP、UCP、工作量、人月、工期、人员数和成本。
4. 导出估算报告。

### 通过用例模型换算 UCP

1. 打开 `http://localhost:8080/use-case-model`。
2. 按“名称, 复杂度”的格式输入参与者和用例。
3. 提交后查看权重、UUCP、UCP 和工作量。
4. 将结果用于 `/estimate` 页面中的项目估算。

## 测试

运行全部测试：

```powershell
mvn test
```

当前测试覆盖的重点包括：

- Spring Boot 上下文启动。
- 首页、分析页、估算页、报告页和用例模型页控制器。
- 源码目录分析流程。
- 代码行统计。
- 项目估算公式。
- 用例模型解析和计算。
- 报告导出入口。

## 项目文档

- [项目计划](docs/PROJECT_PLAN.md)
- [系统设计](docs/SYSTEM_DESIGN.md)
- [使用说明](docs/USAGE.md)
- [测试说明](docs/TESTING.md)

## 目录结构

```text
software-metric
├── docs
│   ├── PROJECT_PLAN.md
│   ├── SYSTEM_DESIGN.md
│   ├── USAGE.md
│   └── TESTING.md
├── examples
│   └── sample-java-project
├── src
│   ├── main
│   │   ├── java/com/example/softwaremetric
│   │   └── resources
│   │       ├── static
│   │       └── templates
│   └── test
│       └── java/com/example/softwaremetric
├── pom.xml
└── README.md
```

## 当前限制与后续扩展

- 当前源码解析主要面向 Java 项目，暂不支持 C/C++、Python、JavaScript 等其他语言。
- ASTParser 当前关闭 binding resolve，因此依赖识别主要基于语法节点文本，跨模块精确类型解析仍可继续增强。
- PDF 报告已支持导出，但复杂表格和中文字体排版仍有继续优化空间。
- 用例模型当前采用文本结构化输入，后续可以扩展为可视化用例建模或导入设计文档。
- 风险等级采用阈值规则，后续可以根据更多项目样本校准阈值或加入趋势分析。

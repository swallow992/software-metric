document.documentElement.dataset.ready = "true";

(function initAnalysisCharts() {
    const chartRoots = Array.from(document.querySelectorAll("[data-analysis-charts]"));

    if (chartRoots.length === 0) {
        return;
    }

    if (!window.echarts) {
        chartRoots.forEach((root) => {
            Array.from(root.querySelectorAll("[data-chart]")).forEach((node) => {
                setChartMessage(node, "ECharts 资源未加载，表格数据仍可查看");
            });
        });
        return;
    }

    const palette = ["#2266d8", "#0f8f7b", "#d88a22", "#7c3aed", "#d64545", "#4b5563"];
    const chartInstances = [];

    chartRoots.forEach((root) => {
        const fileMetrics = readFileMetrics(root);
        const typeMetrics = readTypeMetrics(root);

        renderChart(root, "lineDistribution", createLineDistributionOption(root, palette), chartInstances);
        renderChart(root, "fileCodeLines", createFileCodeLinesOption(fileMetrics, palette), chartInstances);
        renderChart(root, "ckMetrics", createCkMetricsOption(typeMetrics, palette), chartInstances);
        renderChart(root, "riskDistribution", createRiskDistributionOption(typeMetrics, palette), chartInstances);
    });

    let resizeTimer;
    window.addEventListener("resize", () => {
        window.clearTimeout(resizeTimer);
        resizeTimer = window.setTimeout(() => {
            chartInstances.forEach((chart) => chart.resize());
        }, 120);
    });
})();

function renderChart(root, chartName, option, chartInstances) {
    const chartNode = root.querySelector(`[data-chart="${chartName}"]`);

    if (!chartNode) {
        return;
    }

    if (!option) {
        setChartMessage(chartNode, "暂无可视化数据");
        return;
    }

    const chart = window.echarts.init(chartNode);
    chart.setOption(option);
    chartInstances.push(chart);
}

function readFileMetrics(root) {
    return Array.from(root.querySelectorAll("[data-file-metric]")).map((node) => ({
        name: node.dataset.name || "未知文件",
        totalLines: toNumber(node.dataset.totalLines),
        codeLines: toNumber(node.dataset.codeLines),
        commentLines: toNumber(node.dataset.commentLines),
        blankLines: toNumber(node.dataset.blankLines)
    }));
}

function readTypeMetrics(root) {
    return Array.from(root.querySelectorAll("[data-type-metric]")).map((node) => ({
        name: node.dataset.name || node.dataset.qualifiedName || "未知类型",
        qualifiedName: node.dataset.qualifiedName || node.dataset.name || "未知类型",
        wmc: toNumber(node.dataset.wmc),
        dit: toNumber(node.dataset.dit),
        noc: toNumber(node.dataset.noc),
        cbo: toNumber(node.dataset.cbo),
        rfc: toNumber(node.dataset.rfc),
        lcom: toNumber(node.dataset.lcom),
        risk: node.dataset.risk || "未计算"
    }));
}

function createLineDistributionOption(root, palette) {
    const data = [
        {name: "有效代码行", value: toNumber(root.dataset.codeLines)},
        {name: "注释行", value: toNumber(root.dataset.commentLines)},
        {name: "空行", value: toNumber(root.dataset.blankLines)}
    ].filter((item) => item.value > 0);

    if (data.length === 0) {
        return null;
    }

    return {
        color: palette,
        tooltip: {trigger: "item"},
        legend: {bottom: 0, left: "center"},
        series: [{
            name: "代码行分布",
            type: "pie",
            radius: ["44%", "70%"],
            center: ["50%", "42%"],
            avoidLabelOverlap: true,
            data
        }]
    };
}

function createFileCodeLinesOption(fileMetrics, palette) {
    const data = fileMetrics
        .slice()
        .sort((left, right) => right.codeLines - left.codeLines)
        .slice(0, 8)
        .reverse();

    if (data.length === 0) {
        return null;
    }

    return {
        color: [palette[1]],
        grid: {left: 96, right: 18, top: 18, bottom: 24},
        tooltip: {trigger: "axis", axisPointer: {type: "shadow"}},
        xAxis: {type: "value", minInterval: 1},
        yAxis: {
            type: "category",
            data: data.map((item) => shortLabel(item.name)),
            axisLabel: {width: 86, overflow: "truncate"}
        },
        series: [{
            name: "有效代码行",
            type: "bar",
            barMaxWidth: 22,
            data: data.map((item) => item.codeLines)
        }]
    };
}

function createCkMetricsOption(typeMetrics, palette) {
    const data = typeMetrics
        .slice()
        .sort((left, right) => metricWeight(right) - metricWeight(left))
        .slice(0, 8);

    if (data.length === 0) {
        return null;
    }

    return {
        color: [palette[0], palette[1], palette[2], palette[3]],
        legend: {top: 0},
        grid: {left: 42, right: 18, top: 48, bottom: 54},
        tooltip: {trigger: "axis"},
        xAxis: {
            type: "category",
            data: data.map((item) => item.name),
            axisLabel: {interval: 0, rotate: 24, width: 82, overflow: "truncate"}
        },
        yAxis: {type: "value", minInterval: 1},
        series: [
            {name: "WMC", type: "bar", data: data.map((item) => item.wmc)},
            {name: "CBO", type: "bar", data: data.map((item) => item.cbo)},
            {name: "RFC", type: "bar", data: data.map((item) => item.rfc)},
            {name: "LCOM", type: "bar", data: data.map((item) => item.lcom)}
        ]
    };
}

function createRiskDistributionOption(typeMetrics, palette) {
    if (typeMetrics.length === 0) {
        return null;
    }

    const countByRisk = new Map();
    typeMetrics.forEach((item) => {
        countByRisk.set(item.risk, (countByRisk.get(item.risk) || 0) + 1);
    });

    const orderedRisks = ["低风险", "中风险", "高风险", "未计算"];
    const labels = orderedRisks
        .filter((risk) => countByRisk.has(risk))
        .concat(Array.from(countByRisk.keys()).filter((risk) => !orderedRisks.includes(risk)));

    return {
        color: palette,
        grid: {left: 42, right: 18, top: 18, bottom: 34},
        tooltip: {trigger: "axis", axisPointer: {type: "shadow"}},
        xAxis: {type: "category", data: labels},
        yAxis: {type: "value", minInterval: 1},
        series: [{
            name: "类型数量",
            type: "bar",
            barMaxWidth: 34,
            data: labels.map((risk) => countByRisk.get(risk))
        }]
    };
}

function metricWeight(item) {
    return item.wmc + item.cbo + item.rfc + item.lcom;
}

function shortLabel(value) {
    const parts = String(value).split(/[\\/]/);
    return parts[parts.length - 1] || value;
}

function setChartMessage(node, message) {
    node.classList.add("chart-message");
    node.textContent = message;
}

function toNumber(value) {
    const nextValue = Number(value);
    return Number.isFinite(nextValue) ? nextValue : 0;
}

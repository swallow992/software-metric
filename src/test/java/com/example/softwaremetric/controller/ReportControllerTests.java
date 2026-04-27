package com.example.softwaremetric.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportsMarkdownSourceReport() throws Exception {
        mockMvc.perform(get("/reports/source/markdown")
                        .param("sourcePath", "examples/sample-java-project"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("software-metric-report.md")))
                .andExpect(content().string(containsString("# 软件度量分析报告")))
                .andExpect(content().string(containsString("com.example.sample.AdvancedCalculator")))
                .andExpect(content().string(containsString("| 类型 | WMC | DIT | NOC | CBO | RFC | LCOM | 风险 |")));
    }

    @Test
    void rendersHtmlSourceReport() throws Exception {
        mockMvc.perform(get("/reports/source/html")
                        .param("sourcePath", "examples/sample-java-project"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("软件度量分析报告")))
                .andExpect(content().string(containsString("类级面向对象度量")))
                .andExpect(content().string(containsString("度量图表")))
                .andExpect(content().string(containsString("data-analysis-charts")))
                .andExpect(content().string(containsString("AdvancedCalculator")));
    }

    @Test
    void exportsPdfSourceReport() throws Exception {
        mockMvc.perform(get("/reports/source/pdf")
                        .param("sourcePath", "examples/sample-java-project"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("software-metric-report.pdf")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void exportsMarkdownEstimationReport() throws Exception {
        mockMvc.perform(withDefaultEstimationParams(get("/reports/estimate/markdown")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("software-metric-estimate-report.md")))
                .andExpect(content().string(containsString("# 软件项目估算报告")))
                .andExpect(content().string(containsString("## 功能点估算")))
                .andExpect(content().string(containsString("## 用例点估算")))
                .andExpect(content().string(containsString("## COCOMO 估算")))
                .andExpect(content().string(containsString("| 功能点 FP | 66.00 |")));
    }

    @Test
    void rendersHtmlEstimationReport() throws Exception {
        mockMvc.perform(withDefaultEstimationParams(get("/reports/estimate/html")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("软件项目估算报告")))
                .andExpect(content().string(containsString("FP 估算参数与结果")))
                .andExpect(content().string(containsString("UCP 估算参数与结果")))
                .andExpect(content().string(containsString("工作量、工期与成本")));
    }

    @Test
    void exportsPdfEstimationReport() throws Exception {
        mockMvc.perform(withDefaultEstimationParams(get("/reports/estimate/pdf")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("software-metric-estimate-report.pdf")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes(StandardCharsets.US_ASCII)));
    }

    private MockHttpServletRequestBuilder withDefaultEstimationParams(MockHttpServletRequestBuilder request) {
        return request
                .param("externalInputs", "4")
                .param("externalOutputs", "3")
                .param("externalInquiries", "2")
                .param("internalLogicalFiles", "2")
                .param("externalInterfaceFiles", "1")
                .param("technicalComplexityDegree", "35")
                .param("simpleActors", "1")
                .param("averageActors", "1")
                .param("complexActors", "1")
                .param("simpleUseCases", "2")
                .param("averageUseCases", "2")
                .param("complexUseCases", "1")
                .param("useCaseTechnicalFactor", "1.0")
                .param("useCaseEnvironmentalFactor", "1.0")
                .param("hoursPerUseCasePoint", "20.0")
                .param("sourceKloc", "5.0")
                .param("cocomoMode", "organic")
                .param("personMonthCost", "12000.0");
    }
}

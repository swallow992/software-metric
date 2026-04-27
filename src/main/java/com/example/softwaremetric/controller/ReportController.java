package com.example.softwaremetric.controller;

import com.example.softwaremetric.model.EstimationInput;
import com.example.softwaremetric.model.EstimationResult;
import com.example.softwaremetric.model.SourceAnalysisResult;
import com.example.softwaremetric.report.EstimationReportService;
import com.example.softwaremetric.report.PdfReportService;
import com.example.softwaremetric.report.SourceAnalysisReportService;
import com.example.softwaremetric.service.EstimationService;
import com.example.softwaremetric.service.SourceAnalysisService;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    private final SourceAnalysisService sourceAnalysisService;
    private final EstimationService estimationService;
    private final SourceAnalysisReportService sourceAnalysisReportService;
    private final EstimationReportService estimationReportService;
    private final PdfReportService pdfReportService;

    public ReportController(
            SourceAnalysisService sourceAnalysisService,
            EstimationService estimationService,
            SourceAnalysisReportService sourceAnalysisReportService,
            EstimationReportService estimationReportService,
            PdfReportService pdfReportService
    ) {
        this.sourceAnalysisService = sourceAnalysisService;
        this.estimationService = estimationService;
        this.sourceAnalysisReportService = sourceAnalysisReportService;
        this.estimationReportService = estimationReportService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/reports/source/html")
    public String sourceHtmlReport(@RequestParam String sourcePath, Model model) {
        model.addAttribute("sourcePath", sourcePath);
        try {
            model.addAttribute("result", sourceAnalysisService.analyze(sourcePath));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "source-report";
    }

    @GetMapping(value = "/reports/source/markdown", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<String> sourceMarkdownReport(@RequestParam String sourcePath) {
        SourceAnalysisResult result = sourceAnalysisService.analyze(sourcePath);
        String markdown = sourceAnalysisReportService.toMarkdown(result);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("software-metric-report.md", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(markdown);
    }

    @GetMapping(value = "/reports/source/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> sourcePdfReport(@RequestParam String sourcePath) {
        SourceAnalysisResult result = sourceAnalysisService.analyze(sourcePath);
        String markdown = sourceAnalysisReportService.toMarkdown(result);
        byte[] pdf = pdfReportService.fromMarkdown("软件度量分析报告", markdown);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("software-metric-report.pdf", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }

    @GetMapping("/reports/estimate/html")
    public String estimateHtmlReport(EstimationInput input, Model model) {
        model.addAttribute("input", input);
        try {
            model.addAttribute("result", estimationService.estimate(input));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "estimate-report";
    }

    @GetMapping(value = "/reports/estimate/markdown", produces = "text/markdown;charset=UTF-8")
    public ResponseEntity<String> estimateMarkdownReport(EstimationInput input) {
        EstimationResult result = estimationService.estimate(input);
        String markdown = estimationReportService.toMarkdown(input, result);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("software-metric-estimate-report.md", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(markdown);
    }

    @GetMapping(value = "/reports/estimate/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> estimatePdfReport(EstimationInput input) {
        EstimationResult result = estimationService.estimate(input);
        String markdown = estimationReportService.toMarkdown(input, result);
        byte[] pdf = pdfReportService.fromMarkdown("软件项目估算报告", markdown);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("software-metric-estimate-report.pdf", StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }
}

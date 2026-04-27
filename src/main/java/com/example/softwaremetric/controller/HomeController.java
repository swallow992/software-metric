package com.example.softwaremetric.controller;

import com.example.softwaremetric.service.ProjectOverviewService;
import com.example.softwaremetric.service.SourceAnalysisService;
import com.example.softwaremetric.service.ZipSourceUploadService;
import java.nio.file.Path;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class HomeController {

    private static final String SAMPLE_SOURCE_PATH = "examples/sample-java-project";

    private final ProjectOverviewService projectOverviewService;
    private final SourceAnalysisService sourceAnalysisService;
    private final ZipSourceUploadService zipSourceUploadService;

    public HomeController(
            ProjectOverviewService projectOverviewService,
            SourceAnalysisService sourceAnalysisService,
            ZipSourceUploadService zipSourceUploadService
    ) {
        this.projectOverviewService = projectOverviewService;
        this.sourceAnalysisService = sourceAnalysisService;
        this.zipSourceUploadService = zipSourceUploadService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("phase", projectOverviewService.currentPhase());
        model.addAttribute("nextSteps", projectOverviewService.nextSteps());
        return "index";
    }

    @GetMapping("/analyze")
    public String analyze(Model model) {
        model.addAttribute("sourcePath", SAMPLE_SOURCE_PATH);
        return "analyze";
    }

    @PostMapping("/analyze")
    public String analyzeSource(@RequestParam String sourcePath, Model model) {
        model.addAttribute("sourcePath", sourcePath);
        try {
            model.addAttribute("result", sourceAnalysisService.analyze(sourcePath));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "analyze";
    }

    @PostMapping("/analyze/upload")
    public String analyzeUploadedSource(@RequestParam("sourceZip") MultipartFile sourceZip, Model model) {
        try {
            Path extractedSourcePath = zipSourceUploadService.extract(sourceZip);
            String sourcePath = extractedSourcePath.toString();
            model.addAttribute("sourcePath", sourcePath);
            model.addAttribute("result", sourceAnalysisService.analyze(sourcePath));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("sourcePath", SAMPLE_SOURCE_PATH);
            model.addAttribute("errorMessage", exception.getMessage());
        }
        return "analyze";
    }
}

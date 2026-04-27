package com.example.softwaremetric.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DocumentationController {

    private static final Path PROJECT_PLAN_PATH = Path.of("docs", "PROJECT_PLAN.md");

    @GetMapping(value = "/docs/PROJECT_PLAN.md", produces = MediaType.TEXT_MARKDOWN_VALUE)
    @ResponseBody
    public ResponseEntity<String> projectPlan() throws IOException {
        if (!Files.exists(PROJECT_PLAN_PATH)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Files.readString(PROJECT_PLAN_PATH, StandardCharsets.UTF_8));
    }
}

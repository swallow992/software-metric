package com.example.softwaremetric.service;

import com.example.softwaremetric.metric.CkMetricCalculator;
import com.example.softwaremetric.metric.SourceLineCounter;
import com.example.softwaremetric.model.JavaTypeInfo;
import com.example.softwaremetric.model.SourceFileMetric;
import com.example.softwaremetric.model.SourceAnalysisResult;
import com.example.softwaremetric.parser.JavaSourceParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class SourceAnalysisService {

    private final JavaSourceParser javaSourceParser;
    private final SourceLineCounter sourceLineCounter;
    private final CkMetricCalculator ckMetricCalculator;

    public SourceAnalysisService(
            JavaSourceParser javaSourceParser,
            SourceLineCounter sourceLineCounter,
            CkMetricCalculator ckMetricCalculator
    ) {
        this.javaSourceParser = javaSourceParser;
        this.sourceLineCounter = sourceLineCounter;
        this.ckMetricCalculator = ckMetricCalculator;
    }

    public SourceAnalysisResult analyze(String sourcePath) {
        Path sourceRoot = resolveSourceRoot(sourcePath);
        List<Path> javaFiles = findJavaFiles(sourceRoot);
        List<SourceFileAnalysis> fileAnalyses = javaFiles.stream()
                .map(javaFile -> analyzeFile(sourceRoot, javaFile))
                .toList();

        List<JavaTypeInfo> parsedTypes = fileAnalyses.stream()
                .flatMap(fileAnalysis -> fileAnalysis.types().stream())
                .toList();
        List<JavaTypeInfo> types = ckMetricCalculator.calculate(parsedTypes)
                .stream()
                .sorted(Comparator.comparing(JavaTypeInfo::qualifiedName))
                .toList();
        List<SourceFileMetric> fileMetrics = fileAnalyses.stream()
                .map(SourceFileAnalysis::fileMetric)
                .sorted(Comparator.comparing(SourceFileMetric::sourceFile))
                .toList();

        Set<String> packages = types.stream()
                .map(JavaTypeInfo::packageName)
                .filter(packageName -> packageName != null && !packageName.isBlank())
                .collect(Collectors.toSet());

        int classCount = (int) types.stream().filter(type -> "类".equals(type.kind())).count();
        int interfaceCount = (int) types.stream().filter(type -> "接口".equals(type.kind())).count();
        int methodCount = types.stream().mapToInt(JavaTypeInfo::methodCount).sum();
        int fieldCount = types.stream().mapToInt(JavaTypeInfo::fieldCount).sum();
        int totalLines = fileMetrics.stream().mapToInt(SourceFileMetric::totalLines).sum();
        int blankLines = fileMetrics.stream().mapToInt(SourceFileMetric::blankLines).sum();
        int commentLines = fileMetrics.stream().mapToInt(SourceFileMetric::commentLines).sum();
        int codeLines = fileMetrics.stream().mapToInt(SourceFileMetric::codeLines).sum();
        int totalCyclomaticComplexity = types.stream()
                .mapToInt(JavaTypeInfo::totalCyclomaticComplexity)
                .sum();
        double averageMethodCyclomaticComplexity = methodCount == 0
                ? 0
                : (double) totalCyclomaticComplexity / methodCount;

        return new SourceAnalysisResult(
                sourceRoot.toString(),
                javaFiles.size(),
                packages.size(),
                classCount,
                interfaceCount,
                methodCount,
                fieldCount,
                totalLines,
                blankLines,
                commentLines,
                codeLines,
                totalCyclomaticComplexity,
                averageMethodCyclomaticComplexity,
                fileMetrics,
                types
        );
    }

    private Path resolveSourceRoot(String sourcePath) {
        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalArgumentException("源码路径不能为空。");
        }

        try {
            Path path = Path.of(sourcePath.trim()).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("源码路径不存在：" + path);
            }
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("源码路径必须是目录：" + path);
            }
            return path;
        } catch (InvalidPathException exception) {
            throw new IllegalArgumentException("源码路径格式不正确：" + sourcePath, exception);
        }
    }

    private List<Path> findJavaFiles(Path sourceRoot) {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("扫描源码目录失败：" + sourceRoot, exception);
        }
    }

    private SourceFileAnalysis analyzeFile(Path sourceRoot, Path javaFile) {
        try {
            String source = Files.readString(javaFile, StandardCharsets.UTF_8);
            SourceFileMetric fileMetric = sourceLineCounter.count(sourceRoot, javaFile, source);
            List<JavaTypeInfo> types = javaSourceParser.parse(sourceRoot, javaFile, source);
            return new SourceFileAnalysis(fileMetric, types);
        } catch (IOException exception) {
            throw new IllegalStateException("读取 Java 文件失败：" + javaFile, exception);
        }
    }

    private record SourceFileAnalysis(
            SourceFileMetric fileMetric,
            List<JavaTypeInfo> types
    ) {
    }
}

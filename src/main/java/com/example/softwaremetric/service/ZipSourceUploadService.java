package com.example.softwaremetric.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ZipSourceUploadService {

    private final Path uploadRoot;

    public ZipSourceUploadService(@Value("${software-metric.upload-root:uploads/source-zips}") String uploadRoot) {
        this.uploadRoot = Path.of(uploadRoot).toAbsolutePath().normalize();
    }

    public Path extract(MultipartFile file) {
        validate(file);

        Path targetRoot = uploadRoot.resolve(UUID.randomUUID().toString()).normalize();
        try {
            Files.createDirectories(targetRoot);
            extractEntries(file.getInputStream(), targetRoot);
            return targetRoot;
        } catch (IOException exception) {
            throw new IllegalStateException("解压源码 zip 失败。", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请先选择 zip 源码包。");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("仅支持上传 .zip 源码包。");
        }
    }

    private void extractEntries(InputStream inputStream, Path targetRoot) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName().replace('\\', '/');
                Path targetPath = targetRoot.resolve(entryName).normalize();
                if (!targetPath.startsWith(targetRoot)) {
                    throw new IllegalArgumentException("zip 包含不安全路径：" + entry.getName());
                }

                if (entry.isDirectory() || entryName.endsWith("/")) {
                    Files.createDirectories(targetPath);
                } else {
                    Path parent = targetPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(zipInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }
}

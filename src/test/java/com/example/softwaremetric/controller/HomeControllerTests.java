package com.example.softwaremetric.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyzesUploadedZipSourcePackage() throws Exception {
        MockMultipartFile sourceZip = new MockMultipartFile(
                "sourceZip",
                "sample-source.zip",
                "application/zip",
                sampleZip()
        );

        mockMvc.perform(multipart("/analyze/upload").file(sourceZip))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UploadedCalculator")))
                .andExpect(content().string(containsString("data-analysis-charts")))
                .andExpect(content().string(containsString("/reports/source/pdf")));
    }

    private byte[] sampleZip() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            zipOutputStream.putNextEntry(new ZipEntry("sample\\src\\"));
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("sample\\src\\UploadedCalculator.java"));
            zipOutputStream.write("""
                    package uploaded;

                    public class UploadedCalculator {
                        public int add(int left, int right) {
                            return left + right;
                        }
                    }
                    """.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }
        return outputStream.toByteArray();
    }
}

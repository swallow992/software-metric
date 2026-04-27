package com.example.softwaremetric.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PdfReportService {

    private static final String[] CJK_FONT_CANDIDATES = {
            "C:/Windows/Fonts/msyh.ttc,0",
            "C:/Windows/Fonts/simsun.ttc,0",
            "C:/Windows/Fonts/simhei.ttf",
            "/System/Library/Fonts/PingFang.ttc,0",
            "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc,0",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0"
    };

    public byte[] fromMarkdown(String title, String markdown) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            FontSet fonts = createFonts();
            document.add(createParagraph(title, fonts.title(), Element.ALIGN_CENTER));
            document.add(new Paragraph(" ", fonts.body()));

            List<String> tableBuffer = new ArrayList<>();
            for (String rawLine : markdown.split("\\R")) {
                String line = rawLine.trim();
                if (line.startsWith("|")) {
                    tableBuffer.add(line);
                    continue;
                }

                flushTable(document, tableBuffer, fonts);
                appendLine(document, line, fonts);
            }
            flushTable(document, tableBuffer, fonts);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new IllegalStateException("PDF 报告生成失败。", exception);
        }
    }

    private void appendLine(Document document, String line, FontSet fonts) throws DocumentException {
        if (line.isEmpty()) {
            document.add(new Paragraph(" ", fonts.body()));
            return;
        }
        if (line.startsWith("# ")) {
            return;
        }
        if (line.startsWith("## ")) {
            Paragraph heading = createParagraph(line.substring(3), fonts.heading(), Element.ALIGN_LEFT);
            heading.setSpacingBefore(10);
            heading.setSpacingAfter(6);
            document.add(heading);
            return;
        }
        if (line.startsWith("- ")) {
            Paragraph bullet = createParagraph("- " + line.substring(2), fonts.body(), Element.ALIGN_LEFT);
            bullet.setIndentationLeft(12);
            document.add(bullet);
            return;
        }
        document.add(createParagraph(line, fonts.body(), Element.ALIGN_LEFT));
    }

    private void flushTable(Document document, List<String> tableBuffer, FontSet fonts) throws DocumentException {
        if (tableBuffer.isEmpty()) {
            return;
        }

        List<List<String>> rows = tableBuffer.stream()
                .filter(line -> !line.matches("\\|\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?"))
                .map(this::parseTableRow)
                .filter(row -> !row.isEmpty())
                .toList();
        tableBuffer.clear();

        if (rows.isEmpty()) {
            return;
        }

        PdfPTable table = new PdfPTable(rows.get(0).size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(10);

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Font font = rowIndex == 0 ? fonts.tableHeader() : fonts.tableBody();
            for (String cellText : rows.get(rowIndex)) {
                PdfPCell cell = new PdfPCell(new Phrase(cellText, font));
                cell.setPadding(5);
                cell.setUseAscender(true);
                cell.setUseDescender(true);
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    private List<String> parseTableRow(String line) {
        String normalized = line;
        if (normalized.startsWith("|")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("|")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        String[] cells = normalized.split("\\|");
        List<String> row = new ArrayList<>(cells.length);
        for (String cell : cells) {
            row.add(cell.trim());
        }
        return row;
    }

    private Paragraph createParagraph(String text, Font font, int alignment) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(alignment);
        paragraph.setLeading(16);
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    private FontSet createFonts() throws IOException, DocumentException {
        BaseFont baseFont = createBaseFont();
        return new FontSet(
                new Font(baseFont, 18, Font.BOLD),
                new Font(baseFont, 14, Font.BOLD),
                new Font(baseFont, 10, Font.NORMAL),
                new Font(baseFont, 9, Font.BOLD),
                new Font(baseFont, 9, Font.NORMAL)
        );
    }

    private BaseFont createBaseFont() throws IOException, DocumentException {
        for (String candidate : CJK_FONT_CANDIDATES) {
            if (Files.exists(fontPath(candidate))) {
                return BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }

    private Path fontPath(String candidate) {
        String path = candidate.contains(",") ? candidate.substring(0, candidate.indexOf(',')) : candidate;
        return Path.of(path);
    }

    private record FontSet(
            Font title,
            Font heading,
            Font body,
            Font tableHeader,
            Font tableBody
    ) {
    }
}

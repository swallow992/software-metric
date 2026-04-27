package com.example.softwaremetric.metric;

import com.example.softwaremetric.model.SourceFileMetric;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class SourceLineCounter {

    public SourceFileMetric count(Path sourceRoot, Path javaFile, String source) {
        LineState state = new LineState();

        source.lines().forEach(line -> countLine(line, state));

        String sourceFile = sourceRoot.relativize(javaFile).toString().replace('\\', '/');
        return new SourceFileMetric(
                sourceFile,
                state.totalLines,
                state.blankLines,
                state.commentLines,
                state.codeLines
        );
    }

    private void countLine(String line, LineState state) {
        state.totalLines++;
        if (line.isBlank()) {
            state.blankLines++;
        }

        LineScanResult scanResult = scanLine(line, state.inBlockComment);
        state.inBlockComment = scanResult.inBlockComment();

        if (scanResult.hasComment()) {
            state.commentLines++;
        }
        if (scanResult.hasCode()) {
            state.codeLines++;
        }
    }

    private LineScanResult scanLine(String line, boolean startsInBlockComment) {
        boolean inBlockComment = startsInBlockComment;
        boolean hasCode = false;
        boolean hasComment = startsInBlockComment;
        int index = 0;

        while (index < line.length()) {
            if (inBlockComment) {
                int blockEnd = line.indexOf("*/", index);
                if (blockEnd < 0) {
                    return new LineScanResult(hasCode, true, true);
                }
                inBlockComment = false;
                index = blockEnd + 2;
                continue;
            }

            int commentStart = findNextCommentStart(line, index);
            if (commentStart < 0) {
                if (!line.substring(index).isBlank()) {
                    hasCode = true;
                }
                break;
            }

            if (!line.substring(index, commentStart).isBlank()) {
                hasCode = true;
            }
            hasComment = true;

            if (line.startsWith("//", commentStart)) {
                break;
            }

            inBlockComment = true;
            index = commentStart + 2;
        }

        return new LineScanResult(hasCode, hasComment, inBlockComment);
    }

    private int findNextCommentStart(String line, int startIndex) {
        boolean inString = false;
        boolean inCharacter = false;
        boolean escaped = false;

        for (int index = startIndex; index < line.length() - 1; index++) {
            char current = line.charAt(index);
            char next = line.charAt(index + 1);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (current == '\\' && (inString || inCharacter)) {
                escaped = true;
                continue;
            }

            if (current == '"' && !inCharacter) {
                inString = !inString;
                continue;
            }

            if (current == '\'' && !inString) {
                inCharacter = !inCharacter;
                continue;
            }

            if (!inString && !inCharacter && current == '/' && (next == '/' || next == '*')) {
                return index;
            }
        }

        return -1;
    }

    private static final class LineState {
        private int totalLines;
        private int blankLines;
        private int commentLines;
        private int codeLines;
        private boolean inBlockComment;
    }

    private record LineScanResult(
            boolean hasCode,
            boolean hasComment,
            boolean inBlockComment
    ) {
    }
}

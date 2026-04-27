package com.example.softwaremetric.metric;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.softwaremetric.model.SourceFileMetric;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SourceLineCounterTests {

    private final SourceLineCounter sourceLineCounter = new SourceLineCounter();

    @Test
    void countsCodeBlankAndCommentLines() {
        String source = """
                public class Sample {
                    // comment
                    String url = "http://example.com";
                    int a = 1; /* inline */
                    /* block
                       still block */ int b = 2;
                }
                """;

        SourceFileMetric metric = sourceLineCounter.count(
                Path.of("sample"),
                Path.of("sample", "Sample.java"),
                source
        );

        assertThat(metric.totalLines()).isEqualTo(7);
        assertThat(metric.blankLines()).isZero();
        assertThat(metric.commentLines()).isEqualTo(4);
        assertThat(metric.codeLines()).isEqualTo(5);
    }
}

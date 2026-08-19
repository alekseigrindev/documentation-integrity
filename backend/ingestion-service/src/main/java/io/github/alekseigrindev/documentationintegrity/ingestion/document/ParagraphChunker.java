package io.github.alekseigrindev.documentationintegrity.ingestion.document;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits plain Markdown into stable non-empty paragraph chunks.
 */
@Component
public class ParagraphChunker {

    /**
     * Splits content at blank lines while preserving text inside each paragraph.
     *
     * @param resolvedContent resolved Markdown content to split
     * @return non-empty paragraphs in their original order
     */
    public List<String> chunk(String resolvedContent) {
        if (resolvedContent == null || resolvedContent.isBlank()) {
            return List.of();
        }

        List<String> paragraphs = new ArrayList<>();
        StringBuilder currentParagraph = new StringBuilder();

        resolvedContent.lines().forEach(line -> {
            if (line.isBlank()) {
                addParagraph(paragraphs, currentParagraph);
                return;
            }

            if (currentParagraph.length() > 0) {
                currentParagraph.append('\n');
            }

            currentParagraph.append(line);
        });

        addParagraph(paragraphs, currentParagraph);

        return List.copyOf(paragraphs);
    }

    private void addParagraph(List<String> paragraphs, StringBuilder currentParagraph) {
        if (currentParagraph.length() == 0) {
            return;
        }

        paragraphs.add(currentParagraph.toString());
        currentParagraph.setLength(0);
    }
}

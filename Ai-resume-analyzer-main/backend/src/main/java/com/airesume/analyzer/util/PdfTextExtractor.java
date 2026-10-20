package com.airesume.analyzer.util;

import com.airesume.analyzer.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class PdfTextExtractor {

    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new BadRequestException("Encrypted PDFs are not supported");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new BadRequestException("Could not extract any text from the PDF");
            }
            return text;
        } catch (IOException e) {
            log.error("PDF parse failure", e);
            throw new BadRequestException("Failed to parse PDF: " + e.getMessage());
        }
    }
}

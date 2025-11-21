package com.nangoso.pricetracker.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class PdfParserServiceTest {

    @Test
    public void testPdfContent() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/files/items-code.pdf");
        try (InputStream inputStream = resource.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Print first 2000 characters to see the structure
            System.out.println("=== PDF Content (first 2000 chars) ===");
            System.out.println(text.substring(0, Math.min(2000, text.length())));
            System.out.println("\n=== Total length: " + text.length() + " ===");

            // Look for Mastery Book pattern
            System.out.println("\n=== Lines containing 'Mastery Book' ===");
            String[] lines = text.split("\\r?\\n");
            for (int i = 0; i < Math.min(lines.length, 50); i++) {
                if (lines[i].contains("Mastery Book")) {
                    System.out.println(i + ": " + lines[i]);
                }
            }
        }
    }
}

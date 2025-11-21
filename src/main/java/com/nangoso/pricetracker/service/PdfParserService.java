package com.nangoso.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PdfParserService {

    private static final String PDF_PATH = "static/files/items-code.pdf";
    // Pattern: "코드 // [Mastery Book] 이름" 형식에서 코드 추출
    private static final Pattern ITEM_CODE_PATTERN = Pattern.compile("^(\\d{7})\\s*//\\s*\\[Mastery Book\\]");

    /**
     * PDF 파일에서 Mastery Book 아이템 코드를 추출합니다.
     * @return 아이템 코드 리스트
     */
    public List<String> extractMasteryBookCodes() {
        List<String> itemCodes = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource(PDF_PATH);
            try (InputStream inputStream = resource.getInputStream();
                 PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // 텍스트를 줄 단위로 분리
                String[] lines = text.split("\\r?\\n");

                for (String line : lines) {
                    // [Mastery Book]을 포함하는 라인 찾기
                    if (line.contains("[Mastery Book]")) {
                        Matcher matcher = ITEM_CODE_PATTERN.matcher(line.trim());
                        if (matcher.find()) {
                            String itemCode = matcher.group(1);
                            itemCodes.add(itemCode);
                            log.debug("Found Mastery Book code: {} in line: {}", itemCode, line);
                        }
                    }
                }

                log.info("Total {} Mastery Book codes extracted from PDF", itemCodes.size());
            }
        } catch (IOException e) {
            log.error("Failed to read PDF file: {}", PDF_PATH, e);
        }

        return itemCodes;
    }
}

package com.nangoso.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TXT 파일에서 아이템 코드를 파싱하는 서비스
 */
@Service
@Slf4j
public class ItemCodeParserService {

    private static final String TXT_PATH = "static/files/item-codes.txt";
    // Pattern: "코드 // 설명" 형식에서 코드만 추출
    private static final Pattern ITEM_CODE_PATTERN = Pattern.compile("^(\\d{7})\\s*//");

    /**
     * TXT 파일에서 아이템 코드를 추출합니다.
     * @return 아이템 코드 리스트
     */
    public List<String> extractItemCodes() {
        List<String> itemCodes = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource(TXT_PATH);

            try (InputStream inputStream = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();

                    // 빈 줄이나 주석 무시
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    // 아이템 코드 추출
                    Matcher matcher = ITEM_CODE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String itemCode = matcher.group(1);
                        itemCodes.add(itemCode);
                        log.debug("Found item code: {} at line {}", itemCode, lineNumber);
                    } else {
                        log.warn("Invalid format at line {}: {}", lineNumber, line);
                    }
                }

                log.info("Total {} item codes extracted from TXT file", itemCodes.size());
            }
        } catch (IOException e) {
            log.error("Failed to read TXT file: {}", TXT_PATH, e);
        }

        return itemCodes;
    }
}

package com.nangoso.pricetracker.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

public class WebScrapingTest {

    @Test
    public void testWebStructure() throws Exception {
        // 실제 아이템 페이지 크롤링
        String url = "https://mapleland.gg/item/2290053";

        Document doc = Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get();

        System.out.println("=== Page Title ===");
        System.out.println(doc.title());

        System.out.println("\n=== All H1 Tags ===");
        Elements h1s = doc.select("h1");
        for (Element h1 : h1s) {
            System.out.println("H1: " + h1.text());
        }

        System.out.println("\n=== All H2 Tags ===");
        Elements h2s = doc.select("h2");
        for (Element h2 : h2s) {
            System.out.println("H2: " + h2.text());
        }

        System.out.println("\n=== All Images (first 5) ===");
        Elements imgs = doc.select("img");
        int count = 0;
        for (Element img : imgs) {
            System.out.println("IMG src: " + img.attr("src") + " alt: " + img.attr("alt"));
            if (++count >= 5) break;
        }

        System.out.println("\n=== All divs with class containing 'item' (first 10) ===");
        Elements itemDivs = doc.select("div[class*=item]");
        count = 0;
        for (Element div : itemDivs) {
            System.out.println("DIV class: " + div.attr("class") + " text: " + div.text().substring(0, Math.min(50, div.text().length())));
            if (++count >= 10) break;
        }

        System.out.println("\n=== Tabs (buttons or links) ===");
        Elements tabs = doc.select("button, a[role=tab], .tab");
        for (Element tab : tabs) {
            String text = tab.text();
            if (!text.isEmpty() && text.length() < 20) {
                System.out.println("Tab: " + text);
            }
        }

        System.out.println("\n=== Price elements (containing 메소 or 가격) ===");
        Elements priceElements = doc.select("*:contains(메소), *:contains(가격)");
        count = 0;
        for (Element elem : priceElements) {
            if (elem.childrenSize() == 0) { // leaf nodes only
                String text = elem.text().trim();
                if (!text.isEmpty() && text.length() < 100) {
                    System.out.println("Price: " + text);
                    if (++count >= 20) break;
                }
            }
        }
    }
}

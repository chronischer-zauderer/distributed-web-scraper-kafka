package com.scraperproducer.javaproducer.messaging;

public interface HtmlPublisher {
    void publishRawHtml(String url, String htmlContent);
}

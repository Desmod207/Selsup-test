package com.example.crptapi;

import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final RestTemplate restTemplate;

    private final Long timePeriod;

    private final int requestLimit;

    private final Deque<Long> timeDeque = new ConcurrentLinkedDeque<>();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.restTemplate = new RestTemplate();
        this.timePeriod = timeUnit.toChronoUnit().getDuration().toNanos();
        this.requestLimit = requestLimit;
    }

    public void createNewDocument(Document document) {
        URI uri = URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create");

        sendPostRequest(uri, document, Document.class);
    }

    private void sendPostRequest(URI uri, Object body, Class<?> clazz) {
        if (!isLimitationExceeding()) {
            if (timeDeque.size() > requestLimit) {
                timeDeque.pollFirst();
            }
            timeDeque.push(System.nanoTime());

            restTemplate.postForObject(uri, body, clazz);
        }
    }

    // Функция для проверки превышения лимита запросов
    private boolean isLimitationExceeding() {
        return (timeDeque.size() >= requestLimit) && ((System.nanoTime() - timeDeque.getFirst()) < timePeriod);
    }

    record Document (Map<String, String> description,
                     String doc_id,
                     String doc_status,
                     String doc_type,
                     boolean importRequest,
                     String owner_inn,
                     String participant_inn,
                     String producer_inn,
                     String production_date,
                     String production_type,
                     Map<String, String> products,
                     String reg_date,
                     String reg_number) {
    }

    public static void main(String[] args) throws InterruptedException {

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        Document document = new Document(
                Map.of("participantInn", "string"),
                "string",
                "string",
                "string",
                true,
                "string",
                "string",
                "string",
                "string",
                "string",
                Map.of("certificate_document", "string",
                        "certificate_document_date", "2020-01-23",
                        "certificate_document_number", "string",
                        "owner_inn", "string",
                        "producer_inn", "string",
                        "production_date", "2020-01-23",
                        "tnved_code", "string",
                        "uit_code", "string",
                        "uitu_code", "string"),
                "string",
                "string");

        api.createNewDocument(document);
    }

}

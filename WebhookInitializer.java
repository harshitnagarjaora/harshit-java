package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WebhookInitializer implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        // 1. Generate Webhook + Token
        String tokenUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of(
                "name", "Harshit Nagar",
                "regNo", "0827EC221022",
                "email", "harshitnagar221155@acropolis.in"
        );

        HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String webhook = (String) response.getBody().get("webhook");
            String accessToken = (String) response.getBody().get("accessToken");

            System.out.println("✅ Got Webhook: " + webhook);
            System.out.println("✅ Got Token: " + accessToken);

            // Submit the SQL query
            submitFinalQuery(webhook, accessToken);
        } else {
            System.err.println("❌ Failed to fetch webhook/token.");
        }
    }

    private void submitFinalQuery(String webhookUrl, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        headers.set("Authorization", accessToken);

        String sqlQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e1 " +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB " +
                "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e1.EMP_ID DESC";

        Map<String, String> body = Map.of("finalQuery", sqlQuery);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);



    }



}

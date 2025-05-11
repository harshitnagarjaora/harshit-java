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
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
            "name", "Harshit Nagar",
            "regNo", "0827EC221022",
            "email", "harshitnagar221155@acropolis.in"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, String> responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("webhook") && responseBody.containsKey("accessToken")) {
            String webhookUrl = responseBody.get("webhook");
            String accessToken = responseBody.get("accessToken");

            System.out.println("✅ Webhook URL: " + webhookUrl);
            System.out.println("✅ Access Token: " + accessToken);

            submitFinalQuery(webhookUrl, accessToken);
        } else {
            System.err.println("❌ Failed to retrieve webhook and access token. Response: " + responseBody);
        }

    }

    private void submitFinalQuery(String webhookUrl, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        String sqlQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e1 " +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB " +
                "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e1.EMP_ID DESC";

        Map<String, String> body = Map.of("finalQuery", sqlQuery);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(webhookUrl, entity, String.class);
    }
}
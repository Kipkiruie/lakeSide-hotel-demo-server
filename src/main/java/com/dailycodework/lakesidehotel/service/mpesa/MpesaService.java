package com.dailycodework.lakesidehotel.service.mpesa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class MpesaService {

    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortCode;

    @Value("${mpesa.passkey}")
    private String passKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // =========================================
    // GENERATE ACCESS TOKEN
    // =========================================
    public String getAccessToken() {

        try {

            String credentials =
                    consumerKey + ":" + consumerSecret;

            String encodedCredentials =
                    Base64.getEncoder()
                            .encodeToString(
                                    credentials.getBytes(StandardCharsets.UTF_8)
                            );

            HttpHeaders headers = new HttpHeaders();

            headers.set(
                    "Authorization",
                    "Basic " + encodedCredentials
            );

            HttpEntity<String> request =
                    new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials",
                            HttpMethod.GET,
                            request,
                            String.class
                    );

            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonNode =
                    mapper.readTree(response.getBody());

            String accessToken =
                    jsonNode.get("access_token").asText();

            System.out.println("====================================");
            System.out.println("ACCESS TOKEN GENERATED SUCCESSFULLY");
            System.out.println(accessToken);
            System.out.println("====================================");

            return accessToken;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    // =========================================
    // STK PUSH
    // =========================================
    public String stkPush(String phone, String amount) {

        try {

            // Generate token
            String accessToken = getAccessToken();

            if (accessToken == null) {
                return "FAILED TO GENERATE ACCESS TOKEN";
            }

            // Timestamp
            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                            );

            // Password
            String passwordString =
                    shortCode + passKey + timestamp;

            String password =
                    Base64.getEncoder()
                            .encodeToString(
                                    passwordString.getBytes(StandardCharsets.UTF_8)
                            );

            // Debug logs
            System.out.println("====================================");
            System.out.println("MPESA STK PUSH DEBUG");
            System.out.println("SHORTCODE: " + shortCode);
            System.out.println("PASSKEY: " + passKey);
            System.out.println("TIMESTAMP: " + timestamp);
            System.out.println("PASSWORD STRING: " + passwordString);
            System.out.println("ENCODED PASSWORD: " + password);
            System.out.println("PHONE: " + phone);
            System.out.println("AMOUNT: " + amount);
            System.out.println("====================================");

            // STK URL
            String url =
                    "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";

            // Headers
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBearerAuth(accessToken);

            // Request body
            String requestBody =
                    "{"
                            + "\"BusinessShortCode\":\"" + shortCode + "\","
                            + "\"Password\":\"" + password + "\","
                            + "\"Timestamp\":\"" + timestamp + "\","
                            + "\"TransactionType\":\"CustomerPayBillOnline\","
                            + "\"Amount\":\"" + amount + "\","
                            + "\"PartyA\":\"" + phone + "\","
                            + "\"PartyB\":\"" + shortCode + "\","
                            + "\"PhoneNumber\":\"" + phone + "\","
                            + "\"CallBackURL\":\"https://mydomain.com/api/mpesa/callback\","
                            + "\"AccountReference\":\"LakeSideHotel\","
                            + "\"TransactionDesc\":\"Room Booking\""
                            + "}";

            System.out.println("REQUEST BODY:");
            System.out.println(requestBody);

            HttpEntity<String> request =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            String.class
                    );

            System.out.println("====================================");
            System.out.println("MPESA RESPONSE");
            System.out.println(response.getBody());
            System.out.println("====================================");

            return response.getBody();

        } catch (Exception e) {

            e.printStackTrace();

            return "ERROR: " + e.getMessage();
        }
    }
}
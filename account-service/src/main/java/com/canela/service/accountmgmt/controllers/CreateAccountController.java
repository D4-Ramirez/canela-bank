package com.canela.service.accountmgmt.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping(value = "/api/accounts")
public class CreateAccountController {
    @Value("${integrators.data.ip}")
    private String dataIp;

    @Value("${integrators.data.port}")
    private String dataPort;

    @PostMapping(value = "/create-savings" )
    public ResponseEntity<String> createNewAccount(@RequestBody createAccountRequest newAccount) {

        try {
            // GraphQL info
            String url = "http://" + dataIp + ":" + dataPort + "/graphql";
            String operation = "createAccount"; //INSERT OPERATION QUERY HERE
            Random random = new Random();
            String accountId = String.valueOf(random.nextLong(1000000000L));
            String query = "mutation{createAccount(id:\""+accountId+"\",balance:"+newAccount.balance+",user_id:\""+newAccount.userDocument+"\",user_document_type:"+newAccount.typeDocument+"){\n"
                    + "  id\n"
                    + "  balance\n"
                    + "  user_id\n"
                    + "  user_document_type\n"
                    + "}}"; //INSERT QUERY HERE

            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(url);
            URI uri = new URIBuilder(request.getURI())
                    .addParameter("query", query)
                    .build();
            request.setURI(uri);
            HttpResponse response =  client.execute(request);
            InputStream inputResponse = response.getEntity().getContent();
            String actualResponse = new BufferedReader(
                    new InputStreamReader(inputResponse, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            final ObjectNode node = new ObjectMapper().readValue(actualResponse, ObjectNode.class);
            JsonNode account = node.get("data").get(operation);

            // Verify Empty Response
            if(account.isEmpty()) {
                return ResponseEntity.status(HttpURLConnection.HTTP_NOT_FOUND).body("Lo sentimos, hubo un error.");
            }
            // Return response
            else{
                JsonNode UserAccount = node.get("data").get(operation);

                return ResponseEntity.status(HttpURLConnection.HTTP_ACCEPTED).body(UserAccount.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class createAccountRequest {
        private double balance;
        private String userDocument;
        private int typeDocument;

        public double getBalance() {
            return balance;
        }
        public void setBalance(double balance) {
            this.balance = balance;
        }
        public String getUserDocument() {
            return userDocument;
        }
        public void setUserDocument(String userDocument) {
            this.userDocument = userDocument;
        }
        public int getTypeDocument() {
            return typeDocument;
        }
        public void setTypeDocument(int typeDocument) {
            this.typeDocument = typeDocument;
        }
    }
}
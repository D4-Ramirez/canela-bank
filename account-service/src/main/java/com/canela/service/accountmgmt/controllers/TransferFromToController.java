package com.canela.service.accountmgmt.controllers;

import com.canela.service.accountmgmt.exceptions.AccountNotFoundException;
import com.canela.service.accountmgmt.exceptions.DatabaseMutationException;
import com.canela.service.accountmgmt.exceptions.InsufficientFundsException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts/transfer")
public class TransferFromToController {
    @Value("${integrators.data.ip}")
    private String dataIp;

    @Value("${integrators.data.port}")
    private String dataPort;
    private ObjectNode getGraphQLAccount(String account_id, String url) {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();

            String getAccount = String.format("query {\n" +
                    "  getAccountById(id:\"%s\") {\n" +
                    "    id\n" +
                    "    balance\n" +
                    "    user_id\n" +
                    "    user_document_type\n" +
                    "  }\n" +
                    "}", account_id);

            HttpGet requestGraphQL = new HttpGet(url);

            URI uri = new URIBuilder(requestGraphQL.getURI())
                    .addParameter("query", getAccount)
                    .build();
            requestGraphQL.setURI(uri);
            HttpResponse response = client.execute(requestGraphQL);
            InputStream inputResponse = response.getEntity().getContent();
            String actualResponse = new BufferedReader(
                    new InputStreamReader(inputResponse, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            ObjectNode node = new ObjectMapper().readValue(actualResponse, ObjectNode.class);
            return node;

        } catch (Exception e) {

        }
        return null;
    }


    @PostMapping
    public ResponseEntity<String> transfer(@RequestBody TransferToFromEntity transferForm) {
        String url = "http://" + dataIp + ":" + dataPort + "/graphql";

        try {
            ObjectNode originNode = getGraphQLAccount(transferForm.getOrigin_account(), url);
            if(originNode == null)
                System.out.println();

            JsonNode origin_account_json = originNode.get("data");
            if (origin_account_json == null)
                throw new AccountNotFoundException("The account could not be found");
            origin_account_json = origin_account_json.get("getAccountById");

            if(origin_account_json == null)
                throw new AccountNotFoundException("The account could not be found");

            Account origin_account = new ObjectMapper().treeToValue(origin_account_json, Account.class);

            if(origin_account == null)
                throw new AccountNotFoundException("The account could not be found");

            //CHECK IF THE BALANCE ENOUGH TO TRANSFER
            double amount = transferForm.getAmount();
            if (origin_account.balance < amount)
                throw new InsufficientFundsException("The funds of the origin account are not enough for the transfer");


            ObjectNode destinationNode = getGraphQLAccount(transferForm.getDestination_account(), url);

            JsonNode destination_account_json = destinationNode.get("data");
            destination_account_json = destination_account_json.get("getAccountById");

            if(destination_account_json == null)
                throw new AccountNotFoundException("The account could not be found");

            Account destination_account = new ObjectMapper().treeToValue(destination_account_json, Account.class);

            if(destination_account == null)
                throw new AccountNotFoundException("The account could not be found");

            origin_account.balance = origin_account.balance - amount;
            destination_account.balance = destination_account.balance + amount;

            //TRANSACTION
            String movement_id = UUID.randomUUID().toString().replace("-","");
            String movement_origin_account = transferForm.origin_account;
            String movement_destination_account = transferForm.destination_account;
            Double movement_amount = transferForm.amount;
            String movement_date = LocalDateTime.now().toString();

            //Updates values on both tables and adds the movement registry

            String transactionQuery =
                    String.format("mutation {\n" +
                            "  updateOrigin: createAccount(id: \"%s\", balance: %s, user_id: \"%s\", user_document_type: %d){\n" +
                            "    id\n" +
                            "    balance\n" +
                            "    user_id\n" +
                            "    user_document_type\n" +
                            "  }\n" +
                            "  \n" +
                            "  updateDestination: createAccount(id: \"%s\", balance: %s, user_id: \"%s\", user_document_type: %d){\n" +
                            "    id\n" +
                            "    balance\n" +
                            "    user_id\n" +
                            "    user_document_type\n" +
                            "  }\n" +
                            "  \n" +
                            "  createMovement(id: \"%s\", origin_account: \"%s\", destination_account:\"%s\", amount:%s, movement_date:\"%s\") {\n" +
                            "    id\n" +
                            "    origin_account\n" +
                            "    destination_account\n" +
                            "    amount\n" +
                            "    movement_date\n" +
                            "  }\n" +
                            "}",
                            origin_account.id, origin_account.balance, origin_account.user_id, origin_account.user_document_type,
                            destination_account.id, destination_account.balance, destination_account.user_id, destination_account.user_document_type,
                            movement_id, movement_origin_account, movement_destination_account, movement_amount, movement_date
                            );

            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost postGraphQL = new HttpPost(url);

            URI uri = new URIBuilder(postGraphQL.getURI())
                    .addParameter("query", transactionQuery)
                    .build();
            postGraphQL.setURI(uri);
            HttpResponse response = client.execute(postGraphQL);

            InputStream inputResponse = response.getEntity().getContent();
            String actualResponse = new BufferedReader(
                    new InputStreamReader(inputResponse, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            ObjectNode node = new ObjectMapper().readValue(actualResponse, ObjectNode.class);
            JsonNode mutationNode = node.get("data");
            if (mutationNode == null)
                throw new DatabaseMutationException("Failed to realize transaction:\n" + node.toString());
            return ResponseEntity.ok(mutationNode.toString());

        } catch (JsonMappingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (URISyntaxException | InsufficientFundsException | JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DatabaseMutationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    static class Account {
        private String id;
        private double balance;
        private String user_id;
        private int user_document_type;

        public Account(@JsonProperty("id") String id,
                       @JsonProperty("balance") double balance,
                       @JsonProperty("user_id") String user_id,
                       @JsonProperty("user_document_type") int user_document_type) {
            this.id = id;
            this.balance = balance;
            this.user_id = user_id;
            this.user_document_type = user_document_type;
        }
    }

    static class TransferToFromEntity {

        private String origin_account;
        private String destination_account;
        private double amount;

        public TransferToFromEntity() {
        }

        public TransferToFromEntity(String origin_account, String destination_account, double amount) {
            this.origin_account = origin_account;
            this.destination_account = destination_account;
            this.amount = amount;
        }

        public String getOrigin_account() {
            return origin_account;
        }

        public void setOrigin_account(String origin_account) {
            this.origin_account = origin_account;
        }

        public String getDestination_account() {
            return destination_account;
        }

        public void setDestination_account(String destination_account) {
            this.destination_account = destination_account;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }


}



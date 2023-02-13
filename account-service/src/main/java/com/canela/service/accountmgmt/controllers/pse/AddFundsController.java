package com.canela.service.accountmgmt.controllers.pse;

import com.canela.service.accountmgmt.controllers.pse.email.EmailSenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

@RestController
@RequestMapping(value = "/api/accounts")
@Tag(name = "Account", description = "Account REST API")
public class AddFundsController {
    @Value("${integrators.data.ip}")
    private String dataIp;

    @Value("${integrators.data.port}")
    private String dataPort;

    @Value("${integrators.providers.ip}")
    private String providersIp;

    @Value("${integrators.providers.port}")
    private String providersPort;

    @Autowired
    private EmailSenderService service;

    @Operation(summary = "Add funds via PSE", description = "Add funds to the selected account from another bank via PSE", tags = {"Account"})
    @PutMapping(value = "add-funds/pse/{accountId}")
    public ResponseEntity<String> addFunds(@PathVariable(value = "accountId")
                                           @Parameter(name = "Amount id", description = "Number of the account that will be updated", example = "33023227") String id,
                                           @RequestBody PseRequest req) {

        URL url = null;
        try {
            //Connection with PSE
            url = new URL("http://" + providersIp + ":" + providersPort + "/api/prov/pse/approve");
            System.out.println(url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int codeResponse = conn.getResponseCode();

            //If the connection is successful
            if(codeResponse == HttpURLConnection.HTTP_OK){

                //Connection with GraphQL
                URL getAccountUrl = new URL("http://" + dataIp + ":" + dataPort + "/graphql?query=query%7B%0A%20%20getAccountById(id%3A%22"+id+"%22)%7B%0A%20%20%20%20id%0A%20%20%20%20balance%0A%20%20%20%20user_id%2C%0A%20%20%20%20user_document_type%0A%20%20%7D%0A%7D%0A");
                HttpURLConnection connAccount = (HttpURLConnection) getAccountUrl.openConnection();
                connAccount.setRequestMethod("GET");

                //If the connection is successful
                if(connAccount.getResponseCode() == HttpURLConnection.HTTP_OK){
                    //Obtain body information
                    BufferedReader in = new BufferedReader(new InputStreamReader(connAccount.getInputStream()));
                    String inputLine;
                    StringBuilder responseBuff = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        responseBuff.append(inputLine);
                    }
                    in.close();

                    //Parse to JSON the String obtained
                    JSONObject jsonData = new JSONObject(responseBuff.toString());
                    JSONObject jsonGetAccount = new JSONObject(jsonData.get("data").toString());
                    if(jsonGetAccount.get("getAccountById").toString().equals("null")){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró la cuenta");
                    } else {
                        String accountInfo = jsonGetAccount.get("getAccountById").toString();
                        JSONObject jsonAccount = new JSONObject(accountInfo);

                        //Get new balance
                        long newBalance = Integer.parseInt(jsonAccount.get("balance").toString()) + req.getAmount();
                        String user_id = (String) jsonAccount.get("user_id");
                        String user_id_type = jsonAccount.get("user_document_type").toString();

                        //Update balance of the account
                        URL updateAccount = new URL("http://" + dataIp + ":" + dataPort + "/graphql?query=mutation%7B%0A%20%20createAccount%20(id%3A%22"+id+"%22%2C%20balance%3A%20"+newBalance+"%2C%20user_id%3A%20%22"+user_id+"%22%2C%20user_document_type%3A%20"+user_id_type+")%7B%0A%20%20%20%20id%0A%20%20%20%20balance%0A%20%20%20%20user_id%2C%0A%20%20%20%20user_document_type%0A%20%20%7D%0A%7D%0A");
                        HttpURLConnection connUpdate = (HttpURLConnection) updateAccount.openConnection();
                        connUpdate.setRequestMethod("POST");

                        if (connUpdate.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            URL getUserUrl = new URL("http://" + dataIp + ":" + dataPort + "/graphql?query=query{%0A%20%20getUserById(document_type%3A%20"+user_id_type+"%2C%20document%3A%20%22"+user_id+"%22){%0A%20%20%20%20document%2C%0A%20%20%20%20document_type%2C%0A%20%20%20%20name%2C%0A%20%20%20%20last_name%2C%20%0A%20%20%20%20birth_date%2C%20%0A%20%20%20%20address%2C%20%0A%20%20%20%20phone_number%2C%20%0A%20%20%20%20email%0A%20%20}%0A}");
                            HttpURLConnection connUser = (HttpURLConnection) getUserUrl.openConnection();
                            connUser.setRequestMethod("GET");

                            //Obtain body information
                            BufferedReader inUser = new BufferedReader(new InputStreamReader(connUser.getInputStream()));
                            String inputLineUser;
                            StringBuilder responseBuffUser = new StringBuilder();

                            while ((inputLineUser = inUser.readLine()) != null) {
                                responseBuffUser.append(inputLineUser);
                            }
                            inUser.close();

                            //Parse to JSON the String obtained
                            JSONObject jsonDataUser = new JSONObject(responseBuffUser.toString());
                            JSONObject jsonGetUser = new JSONObject(jsonDataUser.get("data").toString());

                            if(jsonGetUser.get("getUserById").toString().equals("null")){
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró el usuario");
                            } else {
                                String userInfo = jsonGetUser.get("getUserById").toString();
                                JSONObject jsonUser = new JSONObject(userInfo);

                                service.sendSimpleEmail(jsonUser.get("email").toString(), "Se registró una recarga en la cuenta " + id + " de " +req.getAmount(), "Recarga PSE");
                                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Monto actualizado");
                            }
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Monto no pudo ser actualizado");
                        }
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Solicitud rechazada");
                }
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("");
    }

    static class PseRequest {
        @Schema(name = "Amount", description = "Amount to add", example = "100000")
        private Long amount;

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }
    }
}

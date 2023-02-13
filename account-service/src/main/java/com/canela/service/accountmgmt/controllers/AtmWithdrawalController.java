package com.canela.service.accountmgmt.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@RestController
@RequestMapping(value = "/api/account")
@Tag(name = "Account", description = "Account REST API")
public class AtmWithdrawalController {
    @Value("${integrators.providers.ip}")
    private String providersIp;

    @Value("${integrators.providers.port}")
    private String providersPort;

    @Operation(summary = "withdraw from  saving account", description = "Using an ATM to withdraw money from savings account", tags = {"Account"})
    @PostMapping(value = "withdraw/atm/{accountId}")
    public ResponseEntity<String> addFunds(@PathVariable(value = "accountId")
                                           @Parameter(name = "Amount id", description = "Number of the account that will be updated", example = "33023227") String id,
                                           @RequestBody AtmRequest req) {
        String response = null;
        try {
            URL url = new URL("http://" + providersIp + ":" + providersPort + "/redaval/solicitud"); //TODO: Change when providers integrator is ready
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            int codeResponse = conn.getResponseCode();
            if(codeResponse == HttpURLConnection.HTTP_OK){
                //TODO: Calculate total fund and update in database
            }
        } catch (MalformedURLException | ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("El body es " + response);
    }

    static class AtmRequest {
        @Schema(name = "Amount", description = "Amount to withdraw", example = "100000")
        private Long amount;

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }
    }

}

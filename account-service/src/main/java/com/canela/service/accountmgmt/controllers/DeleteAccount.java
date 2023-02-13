package com.canela.service.accountmgmt.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.net.*;

@RestController
@RequestMapping("/api/accounts")
public class DeleteAccount {
    @Value("${integrators.data.ip}")
    private String dataIp;

    @Value("${integrators.data.port}")
    private String dataPort;

    @DeleteMapping("/delete/{account}")
    public ResponseEntity<String> delete(@PathVariable String account){
        String url = "http://" + dataIp + ":" + dataPort + "/graphql";
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost requestGraphQL = new HttpPost(url);
            String query = String.format("mutation {\n" +
                    "  deleteAccount(id: \"%s\") {\n" +
                    "    message\n" +
                    "  }\n" +
                    "}", account);
            URI uri = new URIBuilder(requestGraphQL.getURI())
                    .addParameter("query", query)
                    .build();
            requestGraphQL.setURI(uri);
            HttpResponse full_response = client.execute(requestGraphQL);
            int response = full_response.getStatusLine().getStatusCode();
            if(response == HttpStatus.OK.value()){
                return ResponseEntity.status(HttpStatus.OK).body("Eliminado");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(404).body("No se pudo eliminar");
    }
}

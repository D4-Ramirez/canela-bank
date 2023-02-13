package com.canela.service.accountmgmt.controllers;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("account/move-balance")
public class MoveBalance {
    @Value("${integrators.data.ip}")
    private String dataIp;

    @Value("${integrators.data.port}")
    private String dataPort;

    @PostMapping("/{ac1}/{ac2}")
    public ResponseEntity<String> move(@PathVariable String ac1, @PathVariable String ac2){
        try {
            URL url = new URL("http://" + dataIp + ":" + dataPort + "/graphql?query={getAccount(ac1:\"" + ac1 + "\"){id,balance}}");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int response = conn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer res = new StringBuffer();
                String line;
                while((line = reader.readLine()) != null){
                    res.append(line);
                }
                JSONObject object = new JSONObject(res.toString());
                url = new URL("http://" + dataIp + ":" + dataPort + "/graphql?query=mutation{moveBalance(ac1:\""+ ac1 +"\",ac2:\""+ ac2 +"\"){id,origin,destiny,ammount}}");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                int response2 = conn.getResponseCode();
                if(response2 == HttpURLConnection.HTTP_OK) {
                    JSONObject responseJSON = new JSONObject();
                    responseJSON.put("account", ((JSONObject)((JSONObject) object.get("data")).get("getAccount")).get("id"));
                    responseJSON.put("status", "EMPTY");
                    return ResponseEntity.status(HttpStatus.OK).body(responseJSON.toString());
                }
                return ResponseEntity.status(response2).body(conn.getResponseMessage());
            }
            return ResponseEntity.status(response).body(conn.getResponseMessage());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}

package com.canela.service.accountmgmt.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


@RestController
@RequestMapping(value = "/api/accounts")
public class ViewAccountMovementsController {
	@Value("${integrators.data.ip}")
	private String dataIp;

	@Value("${integrators.data.port}")
	private String dataPort;

 	@GetMapping(value = "/viewMovements/{accountId}" )
	public ResponseEntity<String> viewMovements(@PathVariable String accountId) {
		try {
		 String url = "http://" + dataIp + ":" + dataPort + "/graphql";
		 String operation = "getMovementsByOriginAccount";
		 String query = "query{getMovementsByOriginAccount(account_id:\""+accountId+"\"){\n"
				+ "  origin_account\n"
				+ "  destination_account\n"
				+ "  amount\n"
				+ "  movement_date\n"
				+ "	}  \n"
				+ "}";

			 CloseableHttpClient client = HttpClientBuilder.create().build();
				HttpGet requestGraphQL = new HttpGet(url);
				URI uri = new URIBuilder(requestGraphQL.getURI())
						.addParameter("query", query)
						.build();
				requestGraphQL.setURI(uri);
				HttpResponse response =  client.execute(requestGraphQL);
				InputStream inputResponse = response.getEntity().getContent();
				String actualResponse = new BufferedReader(
						new InputStreamReader(inputResponse, StandardCharsets.UTF_8))
						.lines()
						.collect(Collectors.joining("\n"));

				final ObjectNode node = new ObjectMapper().readValue(actualResponse, ObjectNode.class);

				JsonNode Movements = node.get("data").get(operation);

				if(Movements.isEmpty()) {
					 return ResponseEntity.status(HttpURLConnection.HTTP_NOT_FOUND).body("Lo sentimos, hubo un error.");
				}
				else{
					 JsonNode MovementsAccount = node.get("data").get(operation);

					 return ResponseEntity.status(HttpURLConnection.HTTP_OK).body(MovementsAccount.toString());
				}
		} catch (Exception e) {
			throw new RuntimeException(e);

		}
	}
}



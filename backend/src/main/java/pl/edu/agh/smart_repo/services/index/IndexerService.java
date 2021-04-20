package pl.edu.agh.smart_repo.services.index;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class IndexerService {

    private final String index;
    private final HttpClient client;

    @Autowired
    public IndexerService(ConfigurationFactory configurationFactory) {
        log.info("Init indexer service");

        client = HttpClient.newHttpClient();
        index = configurationFactory.getElasticSearchHost() + "/" + configurationFactory.getIndex();

        //TODO move to cfg?
        int number_of_shards = 2;
        int number_of_replicas = 2;

        String json = String.format("{\"settings\":{\"number_of_shards\":%d,\"number_of_replicas\":%d}}",
                number_of_shards, number_of_replicas);

        log.info("Send init index: '" + json + "'");

        HttpRequest request = HttpRequest.newBuilder(URI.create(index))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            log.error("Error while setting up index (index already exists)");
        } catch (InterruptedException | IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (init index)");
        }


        json = "{\n" +
                "  \"properties\" : {\n" +
                "      \"name\" : { \"type\" : \"text\" },\n" +
                "      \"path\" : { \"type\" : \"text\" },\n" +
                "      \"contents\" : { \"type\" : \"text\" },\n" +
                "      \"keywords\" : { \"type\" : \"text\" },\n" +
                "      \"create_date\" : { \"type\" : \"text\" },\n" +
                "      \"modification_date\" : { \"type\" : \"text\" },\n" +
                "      \"language\" : { \"type\" : \"text\" }\n" +
                "  }\n" +
                "}";

        log.info("Send init index mapping: '" + json + "'");

        request = HttpRequest.newBuilder(URI.create(index + "/" + "_mapping"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            log.error("Error while setting up index mapping (mapping already exist)");
        } catch (InterruptedException | IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (init mappings");
        }

    }

    public Result indexDocument(DocumentStructure documentStructure) {

        String json = String.format("{\n" +
                        "  \"name\": \"%s\",\n" +
                        "  \"path\": \"%s\",\n" +
                        "  \"contents\": \"%s\",\n" +
                        "  \"keywords\": \"%s\",\n" +
                        "  \"create_date\": \"%s\",\n" +
                        "  \"modification_date\": \"%s\",\n" +
                        "  \"language\": \"%s\"\n" +
                        "}",
                documentStructure.getByName(DocumentFields.NAME),
                documentStructure.getByName(DocumentFields.PATH),
                documentStructure.getByName(DocumentFields.CONTENTS),
                documentStructure.getByName(DocumentFields.KEYWORDS),
                documentStructure.getByName(DocumentFields.CREATE_DATE),
                documentStructure.getByName(DocumentFields.MODIFICATION_DATE),
                documentStructure.getByName(DocumentFields.LANGUAGE));

        log.info("Send index document: '" + json + "'");

        HttpRequest request = HttpRequest.newBuilder(URI.create(index + "/" + "_doc"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Index response: " + response);
            log.info("Response body: " + response.body());
        } catch (ConnectException e) {
            log.error("Error while indexing document (document already exist)");
            return new Result(ResultType.FAILURE);
        } catch (InterruptedException | IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (index document mappings)");
            return new Result(ResultType.FAILURE);
        }

        return new Result(ResultType.SUCCESS);
    }

    public List<String> search(DocumentFields documentField, String phrase) {
        return Collections.singletonList("xd");
    }
}

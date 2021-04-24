package pl.edu.agh.smart_repo.services.index;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

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
    public IndexerService(ConfigurationFactory configurationFactory,
                          @Value("${elastic.index.number_of_shards}") int number_of_shards,
                          @Value("${elastic.index.number_of_replicas}") int number_of_replicas) {
        log.info("Init indexer service");

        client = HttpClient.newHttpClient();
        index = configurationFactory.getElasticSearchAddress() + "/" + configurationFactory.getIndex();

        createAndSendInitIndexRequest(number_of_shards, number_of_replicas);
        createAndSendInitIndexMappingRequest();
    }

    public Result indexDocument(DocumentStructure documentStructure) {

        String requestBody = createIndexDocumentRequest(documentStructure);
        HttpRequest request = createRequest(index + "/" + "_doc", "POST", requestBody);

        log.info("Send index document: '" + requestBody + "'");

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


    private void createAndSendInitIndexRequest(int number_of_shards, int number_of_replicas) {
        String requestBody = String.format("{\"settings\":{\"number_of_shards\":%d,\"number_of_replicas\":%d}}",
                number_of_shards, number_of_replicas);

        log.info("Send init index: '" + requestBody + "', address: " + index);

        HttpRequest request = createRequest(index, "PUT", requestBody);

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            log.error("Error while setting up index (index already exists)");
        } catch (InterruptedException | IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (init index)");
        }
    }

    private void createAndSendInitIndexMappingRequest() {
        String requestBody = "{\n" +
                "  \"properties\" : {\n" +
                "      \"name\" : { \"type\" : \"text\" },\n" +
                "      \"path\" : { \"type\" : \"text\" },\n" +
                "      \"contents\" : { \"type\" : \"text\" },\n" +
                "      \"keywords\" : { \"type\" : \"text\" },\n" +
                "      \"creation_date\" : { \"type\" : \"text\" },\n" +
                "      \"modification_date\" : { \"type\" : \"text\" },\n" +
                "      \"language\" : { \"type\" : \"text\" }\n" +
                "  }\n" +
                "}";

        log.info("Send init index mapping: '" + requestBody + "'");

        HttpRequest request = createRequest(index + "/" + "_mapping", "PUT", requestBody);

        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            log.error("Error while setting up index mapping (mapping already exist)");
        } catch (InterruptedException | IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (init mappings");
        }
    }

    private HttpRequest createRequest(String uriString, String method, String requestBody) {
        return HttpRequest.newBuilder(URI.create(uriString))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }

    private String createIndexDocumentRequest(DocumentStructure documentStructure) {
        return String.format("{\n" +
                        "  \"name\": \"%s\",\n" +
                        "  \"path\": \"%s\",\n" +
                        "  \"contents\": \"%s\",\n" +
                        "  \"keywords\": \"%s\",\n" +
                        "  \"create_date\": \"%s\",\n" +
                        "  \"modification_date\": \"%s\",\n" +
                        "  \"language\": \"%s\"\n" +
                        "}",
                documentStructure.getByDocumentField(DocumentFields.NAME),
                documentStructure.getByDocumentField(DocumentFields.PATH),
                documentStructure.getByDocumentField(DocumentFields.CONTENTS),
                documentStructure.getByDocumentField(DocumentFields.KEYWORDS),
                documentStructure.getByDocumentField(DocumentFields.CREATION_DATE),
                documentStructure.getByDocumentField(DocumentFields.MODIFICATION_DATE),
                documentStructure.getByDocumentField(DocumentFields.LANGUAGE));
    }
}

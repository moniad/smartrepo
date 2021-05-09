package pl.edu.agh.smart_repo.services.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.common.response.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.directory_tree.util.FileInfoService;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndexerService {
    private final String index;
    private final HttpClient client;
//    private final RestHighLevelClient restHighLevelClient;
    private final FileInfoService fileInfoService;

    @Autowired
    public IndexerService(ConfigurationFactory configurationFactory,
                          FileInfoService fileInfoService,
                          @Value("${elastic.index.number_of_shards}") int number_of_shards,
                          @Value("${elastic.index.number_of_replicas}") int number_of_replicas) {
        this.fileInfoService = fileInfoService;
        log.info("Init indexer service");

        client = HttpClient.newHttpClient();
//        restHighLevelClient = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http"),
//                        new HttpHost("localhost", 9201, "http")));
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

    public Result deleteFileFromIndex(DocumentStructure document) {
        //TODO: add more conditions to query maybe creation date?
        String requestBody = createDeleteFileFromIndexRequest(document);
        HttpRequest request = createRequest(index + "/" + "_delete_by_query", "POST", requestBody);

        log.info("Delete index for document: '" + requestBody + "'");
        var failureMessage = String.format("Cannot delete file %s from index", document.getName());
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Index response: " + response);
            log.info("Response body: " + response.body());
        } catch (ConnectException e) {
            log.error("Error while indexing document: cannot connect with ElasticSearch");
            return new Result(ResultType.FAILURE, failureMessage);
        } catch (InterruptedException | IOException e) {
            log.error("Unexpected error while connecting to ElasticSearch (delete from index)");
            return new Result(ResultType.FAILURE, failureMessage);
        }

        return new Result(ResultType.SUCCESS, String.format("File %s deleted successfully", document.getName()));
    }

    public Option<List<FileInfo>> search(String phrase) {
        String requestBody = createSearchRequest(phrase);
        HttpRequest request = createRequest(index + "/" + "_search", "GET", requestBody);

        log.info("Searching index for: '" + requestBody + "'");
        Option<List<FileInfo>> foundFiles = Option.none();

        try {
            SearchRequest searchRequest = new SearchRequest();
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchQuery("contents", phrase));
            searchRequest.source(searchSourceBuilder);
//            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT); //todo: doesn't work

//            return Option.of(convertToFileInfo(searchResponse.getHits().getHits()));


            HttpResponse<String> stringHttpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            foundFiles = Option.of(mapResponseToFileInfo(stringHttpResponse.body()));
        } catch (JsonProcessingException e) {
            log.warn("Phrase: " + phrase + " not found in any file");
        } catch (IOException e) {
            log.error("Error while searching index for phrase: " + phrase);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return foundFiles;
    }

    private List<FileInfo> convertToFileInfo(SearchHit[] hits) {
        return Arrays.stream(hits).map(hit -> {
            Map<String, Object> stringObjectMap = hit.getSourceAsMap();
            return fileInfoService.getFileByName(stringObjectMap.get("name").toString());
        }).collect(Collectors.toList());
    }


    private List<FileInfo> mapResponseToFileInfo(String responseBody) throws JsonProcessingException {
        return new ObjectMapper().readValue(responseBody, new TypeReference<>() { //todo: parse this format (hits array): {
//                        "hits" : [
//                {
//                    "_index" : "myindex",
//                        "_type" : "_doc",
//                        "_id" : "fhsDBHkBXCVcTa-jRwpc",
//                        "_score" : 0.2915784,
//                        "_source" : {
//                    "name" : "ModelingContinuousSecurity.pdf",
//                            "path" : "null",
//                            "contents" : "Computers & Security 97 (2020) 101967
        });
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
                        "  \"creation_date\": \"%s\",\n" +
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

    private String createDeleteFileFromIndexRequest(DocumentStructure documentStructure) {
        return String.format("{\n" +
                        "  \"query\": {\n" +
                        "    \"match\": {\n" +
                        "      \"path\": \"%s\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                documentStructure.getByDocumentField(DocumentFields.PATH));
    }

    private String createSearchRequest(String phrase) {
        return String.format("{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match\": {\n" +
                "            \"contents\": \"%s\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}", phrase);
    }
}
